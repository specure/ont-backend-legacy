package at.alladin.rmbt.qosadmin.repository;

import at.alladin.rmbt.qosadmin.model.Settings;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author alladin-IT (lb@alladin.at)
 */
public interface SettingsRepository extends JpaRepository<Settings, Long> {

    List<Settings> getByLang(String lang, Sort sort);
}
