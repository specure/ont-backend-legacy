package at.alladin.rmbt.qosadmin.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

public abstract class AbstractRestController<R extends JpaRepository<T, K>, T, K extends Serializable> {

    @Autowired
    R repository;

    Class<T> clazz;

    public AbstractRestController(Class<T> typeClazz) {
        this.clazz = typeClazz;
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<T> getList(Locale locale) {
        return repository.findAll();
    }

    @RequestMapping("/{id}")
    public T getOne(@PathVariable K id, Locale locale) {
        return repository.findOne(id);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable K id, Locale locale) {
        repository.delete(id);
    }

    @RequestMapping(method = RequestMethod.POST)
    public T post(@RequestBody(required = false) final T object, Locale locale) throws Exception {
        if (object == null) {
            return clazz.newInstance();
        } else {
            return repository.save(object);
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public T put(@PathVariable K id, @RequestBody final T object, Locale locale) {
        return repository.save(object);
    }
}
