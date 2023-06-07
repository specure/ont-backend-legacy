package at.alladin.rmbt.qosadmin.repository;

import at.alladin.rmbt.qosadmin.model.TestObjective;
import at.alladin.rmbt.qosadmin.model.types.QoSTestType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author alladin-IT (lb@alladin.at)
 */
public interface TestObjectiveRepository extends JpaRepository<TestObjective, Long> {

    public List<TestObjective> getByTestClass(Long testClass, Sort sort);

    public List<TestObjective> getByTestClassNot(Long testClass, Sort sort);

    public List<TestObjective> getByConcurrencyGroup(Long concurrencyGroup, Sort sort);

    @Query(value = "SELECT t FROM TestObjective t JOIN FETCH t.testServer ts WHERE ts.uid = ?1")
    public List<TestObjective> findByTestServer(Long testServer, Sort sort);

    public List<TestObjective> getByQosTestType(QoSTestType qosTestType, Sort sort);

    public List<TestObjective> findByTestDescriptionKey(String descKey, Sort sort);

    public List<TestObjective> findByTestSummaryKey(String descKey, Sort sort);

    @Query(value = "SELECT COUNT(t.uid), t.test_server FROM qos_test_objective AS t GROUP BY t.test_server ORDER BY t.test_server", nativeQuery = true)
    public List<Object[]> getAllTestServersWithCount();

    @Query(value = "SELECT COUNT(t.uid), t.test_class FROM qos_test_objective AS t GROUP BY t.test_class ORDER BY t.test_class", nativeQuery = true)
    public List<Object[]> getAllTestClassesWithCount();

    @Query(value = "SELECT COUNT(t.uid), t.test_desc FROM qos_test_objective AS t WHERE t.test_class > 0 GROUP BY t.test_desc ORDER BY t.test_desc", nativeQuery = true)
    public List<Object[]> getAllActiveDescKeysWithCount();

    @Query(value = "SELECT COUNT(t.uid), t.test_summary FROM qos_test_objective AS t WHERE t.test_class > 0 GROUP BY t.test_summary ORDER BY t.test_summary", nativeQuery = true)
    public List<Object[]> getAllActiveSummaryKeysWithCount();

    @Query(value = "SELECT COUNT(t.uid), t.test, 'true' FROM qos_test_objective AS t WHERE t.test_class > 0 GROUP BY t.test"
            + " UNION "
            + " SELECT COUNT(t.uid), t.test, 'false' FROM qos_test_objective AS t WHERE t.test_class = 0 GROUP BY t.test", nativeQuery = true)
    public List<Object[]> getAllTestGroupsWithCount();

    @Query(value = "SELECT COUNT(t.uid), t.test FROM qos_test_objective AS t WHERE t.test_class > 0 GROUP BY t.test ORDER BY t.test", nativeQuery = true)
    public List<Object[]> getAllActiveTestGroupsWithCount();

    @Query(value = "SELECT COUNT(t.uid), t.test FROM qos_test_objective AS t WHERE t.test_class = 0 GROUP BY t.test ORDER BY t.test", nativeQuery = true)
    public List<Object[]> getAllInActiveTestGroupsWithCount();
}
