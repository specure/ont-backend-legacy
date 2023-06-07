package at.alladin.rmbt.qosadmin.controller;

import at.alladin.rmbt.qosadmin.controller.EditController.ListElement;
import at.alladin.rmbt.qosadmin.model.TestDescription;
import at.alladin.rmbt.qosadmin.model.TestObjective;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author alladin-IT (lb@alladin.at)
 */
@Controller(value = "/submit/*")
public class SubmitController extends AbstractEditableController {

    @Autowired
    FormattingConversionService formattingService;

    protected static final Logger logger = LoggerFactory.getLogger(SubmitController.class);

    /**
     * @param objType
     * @param objId
     * @param xPath
     * @param data
     * @param locale
     * @param model
     * @return
     * @throws Exception
     */
    @RequestMapping(value = {"/submit/vlist/{objType}/{objId}/{xPath:[a-zA-Z0-9._]*}"})
    public String submitListWithView(@PathVariable("objType") String objType, @PathVariable("objId") Long objId,
                                     @PathVariable("xPath") String xPath, @RequestBody String data, Locale locale, Model model) throws Exception {
        Object item = getListObject(data, objType, objId, locale);
        long uid = 0;
        if ((uid = updateObject(objType, item)) > 0) {
            //reload:
            item = getObject(objType, uid);
        }
        System.out.println(item);
        model.addAttribute("item", item);
        return "components/" + xPath;
    }

    /**
     * @param objType
     * @param objId
     * @param data
     * @param locale
     * @param model
     * @return
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonGenerationException
     */
    @RequestMapping(value = {"/submit/list/{objType}/{objId}", "/submit/list/{objType}/{objId}/{editPath}/{xPath:[a-zA-Z0-9._]*}"})
    public String submitList(@PathVariable("objType") String objType, @PathVariable("objId") Long objId,
                             @PathVariable("xPath") String xPath, @RequestBody String data, Locale locale, Model model) throws Exception {
        Object item = getListObject(data, objType, objId, locale);

        if (item != null) {
            updateObject(objType, item);
        }

        List<ListElement> listMap = new ArrayList<>();

        if (item instanceof TestObjective) {
            listMap.add(new ListElement("test", ((TestObjective) item).getQosTestType().getValue(), "objective/test/"));
            listMap.add(new ListElement("test_class", ((TestObjective) item).getTestClass(), "objective/testclass/"));
            listMap.add(new ListElement("test_server", ((TestObjective) item).getTestServer(), "objective/testserver/"));
            listMap.add(new ListElement("concurrency_group", ((TestObjective) item).getConcurrencyGroup(), "objective/concurrencygroup/"));
            listMap.add(new ListElement("test_desc", ((TestObjective) item).getTestDescriptionKey(), "testdesc/key/"));
            listMap.add(new ListElement("test_summary", ((TestObjective) item).getTestSummaryKey(), "testdesc/key/"));
        } else if (item instanceof TestDescription) {
            listMap.add(new ListElement("key", ((TestDescription) item).getKey(), "objective/test/"));
            listMap.add(new ListElement("value", ((TestDescription) item).getValue(), "objective/testclass/"));
            listMap.add(new ListElement("languageCode", ((TestDescription) item).getLanguageCode(), "objective/testserver/"));
        }
        model.addAttribute("item", item);

        return "components/" + xPath;
    }

    /**
     * @param data
     * @param objType
     * @param objId
     * @param locale
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private Object getListObject(String data, String objType, Long objId, Locale locale) throws Exception {
        Map<String, Object> parameterMap = new HashMap<>();
        List<NameValuePair> params = URLEncodedUtils.parse(data, Charset.forName("UTF-8"));
        for (NameValuePair nameValuePair : params) {
            parameterMap.put(nameValuePair.getName(), nameValuePair.getValue());
        }


        Object item = getObject(objType, objId);

        for (Entry<String, Object> mapEntry : parameterMap.entrySet()) {
            Field f = item.getClass().getDeclaredField(mapEntry.getKey());
            if (f != null) {
                if (!f.isAccessible()) {
                    f.setAccessible(true);
                }

                if (formattingService.canConvert(String.class, f.getType())) {
                    f.set(item, formattingService.convert(mapEntry.getValue(), f.getType()));
                } else if (f.getType().isEnum()) {
                    f.set(item, Enum.valueOf(f.getType().asSubclass(Enum.class), (String) mapEntry.getValue()));
                } else {
                    try {
                        Constructor<?> c;
                        c = f.getType().getConstructor(String.class);
                        Object o = c.newInstance(mapEntry.getValue());
                        f.set(item, o);
                    } catch (NoSuchMethodException e1) {
                        //no constructor with String parameter found?
                        try {
                            //look for valueOf method
                            Method valueOfMethod = f.getType().getDeclaredMethod("valueOf", String.class);
                            System.out.println(mapEntry.getValue());
                            f.set(item, valueOfMethod.invoke(item, mapEntry.getValue()));
                        } catch (NoSuchMethodException e2) {
                            //not found either? try to set the value
                            e2.printStackTrace();
                            f.set(item, mapEntry.getValue());
                        } catch (Exception e) {
                            f.set(item, null);
                        }
                    }
                }
            }
        }

        return item;
    }

    /**
     * @param objType
     * @param objId
     * @param editPath
     * @param data
     * @param locale
     * @param model
     * @return
     * @throws Exception
     */
    @RequestMapping("/submit/item/{objType}/{objId}/{editPath}/{xPath:[a-zA-Z0-9._]*}")
    public String submitItem(@PathVariable("objType") String objType, @PathVariable("objId") Long objId,
                             @PathVariable("editPath") String editPath, @PathVariable("xPath") String xPath,
                             @RequestParam("data") String data, Locale locale, Model model) throws Exception {
        Object item = getObject(objType, objId);

        try {
            Field f = item.getClass().getDeclaredField(editPath);
            if (f != null) {
                f.set(item, data);
                updateObject(objType, item);
                model.addAttribute("item", item);
                //return String.valueOf(f.get(item));
            } else {
                return "ERROR: object not found";
            }
        } catch (Exception e) {
            logger.error("ERROR", e);
            return "ERROR: " + e.getLocalizedMessage();
        }

        return "components/" + xPath;
    }
}
