package at.alladin.rmbt.qosadmin.controller;

import at.alladin.rmbt.qosadmin.model.*;
import at.alladin.rmbt.qosadmin.repository.*;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author alladin-IT (lb@alladin.at)
 */
public class AbstractEditableController {

    @Autowired
    TestDescriptionRepository testDescriptionRepository;

    @Autowired
    TestObjectiveRepository testObjectiveRepository;

    @Autowired
    TestServerRepository testServerRepository;

    @Autowired
    SettingsRepository settingsRepository;

    @Autowired
    NewsRepository newsRepository;

    /**
     * @param objType
     * @param objId
     * @return
     */
    protected Object getObject(String objType, Long objId) {
        Object item = null;
        switch (objType) {
            case "objective":
                item = testObjectiveRepository.findOne(objId);
                if (item == null) {
                    item = new TestObjective();
                }
                break;
            case "testdesc":
                item = testDescriptionRepository.findOne(objId);
                if (item == null) {
                    item = new TestDescription();
                }
                break;
            case "testserver":
                item = testServerRepository.findOne(objId);
                if (item == null) {
                    item = new TestServer();
                }
                break;
            case "settings":
                item = settingsRepository.findOne(objId);
                if (item == null) {
                    item = new Settings();
                }
                break;
            case "news":
                item = newsRepository.findOne(objId);
                if (item == null) {
                    item = new News();
                }
                break;
            default:
                throw new UnsupportedOperationException();
        }

        return item;
    }

    /**
     * @param objType
     * @param o
     * @return
     */
    protected long updateObject(String objType, Object o) {
        long uid = 0;
        switch (objType) {
            case "objective":
                testObjectiveRepository.save((TestObjective) o);
                uid = ((TestObjective) o).getUid();
                break;
            case "testdesc":
                testDescriptionRepository.save((TestDescription) o);
                uid = ((TestDescription) o).getUid();
                break;
            case "testserver":
                testServerRepository.save((TestServer) o);
                uid = ((TestServer) o).getUid();
                break;
            case "settings":
                settingsRepository.save((Settings) o);
                uid = ((Settings) o).getUid();
                break;
            case "news":
                newsRepository.save((News) o);
                uid = ((News) o).getUid();
                break;
            default:
                throw new UnsupportedOperationException();
        }

        return uid;
    }

    /**
     * @param objType
     * @param o
     * @return
     */
    protected boolean deleteObject(String objType, Object o) {
        switch (objType) {
            case "objective":
                testObjectiveRepository.delete((TestObjective) o);
                break;
            case "testdesc":
                testDescriptionRepository.delete((TestDescription) o);
                break;
            case "testserver":
                testServerRepository.delete((TestServer) o);
                break;
            case "settings":
                settingsRepository.delete((Settings) o);
                break;
            case "news":
                newsRepository.delete((News) o);
                break;
            default:
                throw new UnsupportedOperationException();
        }

        return true;
    }
}
