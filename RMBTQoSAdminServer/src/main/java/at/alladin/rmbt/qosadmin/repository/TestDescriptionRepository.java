package at.alladin.rmbt.qosadmin.repository;

import at.alladin.rmbt.qosadmin.model.TestDescription;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author alladin-IT (lb@alladin.at)
 */
public interface TestDescriptionRepository extends JpaRepository<TestDescription, Long> {

    public List<TestDescription> findByLanguageCode(String languageCode, Sort sort);

    public List<TestDescription> findByKey(String key, Sort sort);

    public List<TestDescription> findByKeyStartsWith(String key, Sort sort);

    @Query("SELECT DISTINCT t.key FROM TestDescription t")
    public List<String> findAllKeys();

    @Query("SELECT COUNT(t) FROM TestDescription t")
    public Long countByUid();
}
