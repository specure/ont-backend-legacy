package at.alladin.rmbt.qosadmin.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Locale;

/**
 * @author alladin-IT (lb@alladin.at)
 */
@Controller("/delete/*")
public class DeleteController extends AbstractEditableController {

    private static final Logger logger = LoggerFactory.getLogger(EditController.class);

    /**
     * @param objType
     * @param objId
     * @param editPath
     * @param data
     * @param locale
     * @param model
     * @return
     */
    @ResponseBody
    @RequestMapping("/delete/item/{objType}/{objId}")
    public String submitItem(@PathVariable("objType") String objType, @PathVariable("objId") Long objId, Locale locale, Model model) {
        try {
            Object item = getObject(objType, objId);
            if (item != null) {
                logger.info("deleting object: {}", item);
                if (deleteObject(objType, item)) {
                    return "OK";
                }
            }
        } catch (Exception e) {
            logger.error("ERROR", e);
            return "ERROR:\n" + e.getMessage();
        }

        return "ERROR";
    }
}
