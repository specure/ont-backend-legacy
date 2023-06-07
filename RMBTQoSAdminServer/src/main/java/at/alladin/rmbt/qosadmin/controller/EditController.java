package at.alladin.rmbt.qosadmin.controller;

import at.alladin.rmbt.qosadmin.model.TestDescription;
import at.alladin.rmbt.qosadmin.model.TestObjective;
import at.alladin.rmbt.qosadmin.model.types.QoSTestType;
import at.alladin.rmbt.qosadmin.repository.TestDescriptionRepository;
import at.alladin.rmbt.qosadmin.repository.TestObjectiveRepository;
import at.alladin.rmbt.qosadmin.repository.TestServerRepository;
import at.alladin.rmbt.qosadmin.util.TestServerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * @author alladin-IT (lb@alladin.at)
 */
@Controller("/edit/*")
public class EditController {

    public static class OptionsList implements Serializable {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        List<String> options;
        String selected;

        public OptionsList(List<String> options, String selected) {
            this.options = options;
            this.selected = selected;
        }

        public List<String> getOptions() {
            return options;
        }

        public void setOptions(List<String> options) {
            this.options = options;
        }

        public String getSelected() {
            return selected;
        }

        public void setSelected(String selected) {
            this.selected = selected;
        }
    }

    public static class ListElement implements Serializable {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        String key;
        String value;

        /**
         * can be a list to make the view render it as an <option>
         */
        Object tag;

        /**
         * the tag type identifier
         */
        final String tagType;

        public ListElement(String key, Object value) {
            this(key, value, "");
        }

        public ListElement(String key, Object value, Object tag) {
            this.key = key;
            this.value = String.valueOf(value);
            this.tag = tag;

            if (OptionsList.class.isAssignableFrom(tag.getClass())) {
                this.tagType = "options";
            } else if (Collection.class.isAssignableFrom(tag.getClass())) {
                this.tagType = "collection";
            } else if (tag instanceof String) {
                this.tagType = "string";
            } else {
                this.tagType = "object";
            }
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public Object getTag() {
            return tag;
        }

        public void setTag(Object tag) {
            this.tag = tag;
        }

        public String getTagType() {
            return tagType;
        }
    }

    protected static final Logger logger = LoggerFactory.getLogger(EditController.class);

    @Autowired
    TestDescriptionRepository testDescriptionRepository;

    @Autowired
    TestObjectiveRepository testObjectiveRepository;

    @Autowired
    TestServerRepository testServerRepository;

    /**
     * @param objType
     * @param objId
     * @param locale
     * @param model
     * @return
     */
    @RequestMapping("/edit/get/{objType}/{objId}/{editPath}")
    public String getEditItem(@PathVariable("objType") String objType, @PathVariable("objId") Long objId,
                              @PathVariable("editPath") String editPath, Locale locale, Model model) {
        Object item = null;
        List<ListElement> listMap = new ArrayList<>();

        switch (objType) {
            case "objective":
                item = testObjectiveRepository.findOne(objId);
                if (item == null) {
                    item = new TestObjective();
                }
                model.addAttribute("id", ((TestObjective) item).getUid());

                List<String> keyList = testDescriptionRepository.findAllKeys();
                List<Object[]> testServerAbstractList = testServerRepository.getAllOnlyUidAndName();
                List<String> testServerList = new ArrayList<String>();
                for (Object[] tsEntry : testServerAbstractList) {
                    testServerList.add(tsEntry[0] + " - " + tsEntry[1]);
                }

                listMap.add(new ListElement("qosTestType", "test",
                        new OptionsList(QoSTestType.NAME_LIST, ((TestObjective) item).getQosTestType().toString())));
                listMap.add(new ListElement("testClass", "test_class"));
                listMap.add(new ListElement("testServer", "test_server",
                        new OptionsList(testServerList, TestServerUtil.getTestServerName(((TestObjective) item).getTestServer()))));
                listMap.add(new ListElement("concurrencyGroup", "concurrency_group"));
                listMap.add(new ListElement("testDescriptionKey", "test_desc",
                        new OptionsList(keyList, ((TestObjective) item).getTestDescriptionKey())));
                listMap.add(new ListElement("testSummaryKey", "test_summary",
                        new OptionsList(keyList, ((TestObjective) item).getTestSummaryKey())));
                break;
            case "testdesc":
                item = testDescriptionRepository.findOne(objId);
                if (item == null) {
                    item = new TestDescription();
                }
                model.addAttribute("id", ((TestDescription) item).getUid());
                break;
            default:
                return null;
        }

        model.addAttribute("editPathMap", listMap);
        model.addAttribute("editPath", editPath);
        model.addAttribute("editItem", item);

        if ("multiedit".equals(editPath)) {
            return "components/listbox";
        } else {
            return "components/singlebox";
        }
    }

    @RequestMapping("/edit/add/{objType}/{xPath}")
    public String getNewItem(@PathVariable("objType") String objType, @PathVariable("xPath") String xPath, Locale locale, Model model) {
        return "components/" + xPath;
    }
}
