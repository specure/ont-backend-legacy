package at.alladin.rmbt.qosadmin.model;

import at.alladin.rmbt.qosadmin.model.types.QoSTestType;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * @author alladin-IT (lb@alladin.at)
 */
@Entity
@Table(name = "qos_test_objective")
public class TestObjective implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public final static Pattern PARAM_PATTERN = Pattern.compile("(\"[^\"]*\"=>\"[^\"]*\")");

    public final static String REGEX_PARAM_PATTERN_SEPARATOR = "(\"[^\"]*\"=>\"[^\"]*\")[\\s]*,";

	/*
	 *   uid serial NOT NULL,
		 test qostest NOT NULL,
		 param hstore NOT NULL,
		 test_class integer,
		 results hstore[],
		 test_server integer,
		 concurrency_group integer NOT NULL DEFAULT 0,
		 test_desc text,
		 test_summary text,
	 */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long uid;

    @Column(name = "test")
    @Type(type = "at.alladin.rmbt.qosadmin.model.types.QoSTestTypeEnum")
    QoSTestType qosTestType = QoSTestType.DNS;

    @Column(name = "param")
    @Type(type = "at.alladin.rmbt.qosadmin.model.types.HStoreType")
    String parameters = "";

    @Column(name = "test_class")
    Long testClass;

    @Column(name = "results")
    @Type(type = "at.alladin.rmbt.qosadmin.model.types.HStoreType")
    String objectives = "{}";

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "test_server")
    TestServer testServer;

    @Column(name = "concurrency_group")
    Long concurrencyGroup;

    @Column(name = "test_desc")
    String testDescriptionKey;

    @Column(name = "test_summary")
    String testSummaryKey;

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public QoSTestType getQosTestType() {
        return qosTestType;
    }

    public void setQosTestType(QoSTestType qosTestType) {
        this.qosTestType = qosTestType;
    }

    public Long getTestClass() {
        return testClass;
    }

    public void setTestClass(Long testClass) {
        this.testClass = testClass;
    }

    public String getObjectives() {
        return objectives;
    }

    public void setObjectives(String objectives) {
        this.objectives = objectives;
    }

    public Long getConcurrencyGroup() {
        return concurrencyGroup;
    }

    public TestServer getTestServer() {
        return testServer;
    }

    public void setTestServer(TestServer testServer) {
        this.testServer = testServer;
    }

    public void setConcurrencyGroup(Long concurrencyGroup) {
        this.concurrencyGroup = concurrencyGroup;
    }

    public String getTestDescriptionKey() {
        return testDescriptionKey;
    }

    public void setTestDescriptionKey(String testDescriptionKey) {
        this.testDescriptionKey = testDescriptionKey;
    }

    public String getTestSummaryKey() {
        return testSummaryKey;
    }

    public void setTestSummaryKey(String testSummaryKey) {
        this.testSummaryKey = testSummaryKey;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
                * result
                + ((concurrencyGroup == null) ? 0 : concurrencyGroup.hashCode());
        result = prime * result
                + ((objectives == null) ? 0 : objectives.hashCode());
        result = prime * result
                + ((parameters == null) ? 0 : parameters.hashCode());
        result = prime * result
                + ((qosTestType == null) ? 0 : qosTestType.hashCode());
        result = prime * result
                + ((testClass == null) ? 0 : testClass.hashCode());
        result = prime
                * result
                + ((testDescriptionKey == null) ? 0 : testDescriptionKey
                .hashCode());
        result = prime * result
                + ((testServer == null) ? 0 : testServer.hashCode());
        result = prime * result
                + ((testSummaryKey == null) ? 0 : testSummaryKey.hashCode());
        result = prime * result + ((uid == null) ? 0 : uid.hashCode());
        return result;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TestObjective other = (TestObjective) obj;
        if (concurrencyGroup == null) {
            if (other.concurrencyGroup != null)
                return false;
        } else if (!concurrencyGroup.equals(other.concurrencyGroup))
            return false;
        if (objectives == null) {
            if (other.objectives != null)
                return false;
        } else if (!objectives.equals(other.objectives))
            return false;
        if (parameters == null) {
            if (other.parameters != null)
                return false;
        } else if (!parameters.equals(other.parameters))
            return false;
        if (qosTestType == null) {
            if (other.qosTestType != null)
                return false;
        } else if (!qosTestType.equals(other.qosTestType))
            return false;
        if (testClass == null) {
            if (other.testClass != null)
                return false;
        } else if (!testClass.equals(other.testClass))
            return false;
        if (testDescriptionKey == null) {
            if (other.testDescriptionKey != null)
                return false;
        } else if (!testDescriptionKey.equals(other.testDescriptionKey))
            return false;
        if (testServer == null) {
            if (other.testServer != null)
                return false;
        } else if (!testServer.equals(other.testServer))
            return false;
        if (testSummaryKey == null) {
            if (other.testSummaryKey != null)
                return false;
        } else if (!testSummaryKey.equals(other.testSummaryKey))
            return false;
        if (uid == null) {
            if (other.uid != null)
                return false;
        } else if (!uid.equals(other.uid))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "TestObjective [uid=" + uid + ", qosTestType=" + qosTestType
                + ", parameters=" + parameters + ", testClass=" + testClass
                + ", objectives=" + objectives + ", testServer=" + testServer
                + ", concurrencyGroup=" + concurrencyGroup
                + ", testDescriptionKey=" + testDescriptionKey
                + ", testSummaryKey=" + testSummaryKey + "]";
    }
}
