package at.alladin.rmbt.qosadmin.repository;

import at.alladin.rmbt.qosadmin.model.News;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author alladin-IT (lb@alladin.at)
 */
public interface NewsRepository extends JpaRepository<News, Long> {
}
