package at.alladin.rmbt.qosadmin.repository;

import at.alladin.rmbt.qosadmin.model.TestServer;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author alladin-IT (lb@alladin.at)
 */
public interface TestServerRepository extends JpaRepository<TestServer, Long> {

    public List<TestServer> getByCountry(String country, Sort sort);

    public List<TestServer> getByCity(String city, Sort sort);

    @Query(value = "SELECT t.uid, t.name FROM test_server t ORDER BY t.uid", nativeQuery = true)
    public List<Object[]> getAllOnlyUidAndName();
}
