package at.alladin.rmbt.qosadmin.service.formatter;

import at.alladin.rmbt.qosadmin.model.TestServer;
import at.alladin.rmbt.qosadmin.repository.TestServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author alladin-IT (lb@alladin.at)
 */
@Component
public class TestServerFormatter implements Formatter<TestServer> {

    private final static Pattern ID_PATTERN = Pattern.compile("[0-9]*");

    @Autowired
    @Lazy
    TestServerRepository testServerRepository;

    @Override
    public String print(TestServer object, Locale locale) {
        return object.getUid().toString() + " - " + object.getName();
    }

    @Override
    public TestServer parse(String text, Locale locale) throws ParseException {
        TestServer testServer = null;

        final Matcher m = ID_PATTERN.matcher(text);
        if (m.find()) {
            final long uid = Long.parseLong(m.group(0));
            testServer = testServerRepository.findOne(uid);
        }

        if (testServer == null) {
            throw new ParseException("Could not find test server: '" + text + "'", 0);
        }

        return testServer;
    }

}
