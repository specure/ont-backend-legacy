package at.alladin.rmbt.qosadmin.controller.list;

import at.alladin.rmbt.qosadmin.model.News;
import at.alladin.rmbt.qosadmin.repository.NewsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author alladin-IT (lb@alladin.at)
 */
@Controller("/list/news/*")
public class NewsListController {

    protected static final Logger logger = LoggerFactory.getLogger(NewsListController.class);

    @Autowired
    NewsRepository newsRepository;

    @RequestMapping("/list/news")
    public String showNewsList(Locale locale, Model model) {
        return listNews("", null, locale, model);
    }

    @RequestMapping("/list/news/{searchParam}/{searchString:[a-zA-Z0-9._]*}")
    public String showNewsListBy(@PathVariable("searchParam") String searchParam,
                                 @PathVariable("searchString") String searchString, Locale locale, Model model) {
        return listNews(searchParam, searchString, locale, model);
    }

    /**
     * @param listBy
     * @param parameter
     * @param locale
     * @param model
     * @return
     */
    private String listNews(String listBy, Object parameter, Locale locale, Model model) {
        List<News> list = null;
        String title = "All news";

        switch (listBy) {
            case "uid":
                News object = newsRepository.findOne(Long.valueOf((String) parameter));
                if (object != null) {
                    list = new ArrayList<News>();
                    list.add(object);
                }
                title = "News with UID: " + parameter;
                break;
            default:
                list = newsRepository.findAll(new Sort("uid"));
                title = "All news";
        }

        if (list != null) {
            model.addAttribute("list", list);
        }
        model.addAttribute("title", title);

        return "list/news";
    }
}
