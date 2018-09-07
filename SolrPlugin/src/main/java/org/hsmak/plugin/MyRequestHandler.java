package org.hsmak.plugin;

import org.apache.log4j.Logger;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.hsmak.validation.Main;
import org.hsmak.validation.model.Address;
import org.hsmak.validation.model.Person;
import org.hsmak.validation.validator.PersonValidator;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;

/**
 * Created by hsmak on 12/1/16.
 */

public class MyRequestHandler extends RequestHandlerBase {

    private static Logger LOG = Logger.getLogger(MyRequestHandler.class);

    volatile long numRequests;
    volatile long totalTime;
    volatile long numErrors;
    List<String> words;

    private ClassPathXmlApplicationContext context;

    @Override
    public void init(NamedList params) {

        LOG.debug(getClass() + " @ init(): inittttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt");

//        final ClassLoader properClassLoader = Thread.currentThread().getContextClassLoader();
        final ClassLoader properClassLoader = SolrResourceLoader.class.getClassLoader();

        LOG.debug(properClassLoader.getClass().getSimpleName());
        LOG.debug(properClassLoader.getResource("/"));

        /*ClassLoader cl = ClassLoader.getSystemClassLoader();

        URL[] urls = ((URLClassLoader)cl).getURLs();

        for(URL url: urls){
            LOG.debug(url.getFile());
        }*/

//        final ClassLoader properClassLoader = ClassLoader.getSystemClassLoader();
//        final ClassLoader properClassLoader = SolrResourceLoader.class.getClassLoader();
//        LOG.debug(getClass().getResource("ApplicationContext.xml").getPath());

        /*context = new ClassPathXmlApplicationContext("classpath:*ApplicationContext.xml") {

            protected void initBeanDefinitionReader(XmlBeanDefinitionReader reader) {
                super.initBeanDefinitionReader(reader);
                reader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_AUTO);
                reader.setBeanClassLoader(properClassLoader);
                setClassLoader(null);
            }
        };*/

        context = new ClassPathXmlApplicationContext("classpath:*ApplicationContext.xml");
        context.setClassLoader(properClassLoader);
        PersonValidator personValidator = (PersonValidator)context.getBean("personValidator");

        Address address = new Address();
        Person person = new Person();

        DataBinder dataBinder = new DataBinder(person);
        dataBinder.setValidator(personValidator);
        dataBinder.validate();

        BindingResult bindingResult = dataBinder.getBindingResult();
        LOG.debug(bindingResult);

        words = ((NamedList) params.get("words")).getAll("word");

        if (words.isEmpty()) {
            throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,
                    "Need to specify at least one word in requestHandler config!");
        }

        super.init(params); //pass the rest of the init up

    }

    @Override
    public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {

        LOG.debug(getClass() + " @ handleRequestBody(): handleRequestBodyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy");

        PersonValidator personValidator = (PersonValidator)context.getBean("personValidator");

        Address address = new Address();
        Person person = new Person();

        DataBinder dataBinder = new DataBinder(person);
        dataBinder.setValidator(personValidator);
        dataBinder.validate();

        BindingResult bindingResult = dataBinder.getBindingResult();
        LOG.debug(bindingResult);




        numRequests++;
        long startTime = System.currentTimeMillis();

        try {
            HashMap<String, Double> counts = new HashMap<String, Double>();

            SolrParams params = req.getParams();
            String q = params.get(CommonParams.Q); //get the q param from url

            for (String string : q.split(" ")) {
                if (words.contains(string)) {
                    Double oldcount = counts.containsKey(string) ? counts.get(string) : 0.0;
                    counts.put(string, oldcount + 1);


                    NamedList<Double> results = new NamedList<Double>();
                    for (String word : words) {
                        results.add(word, counts.get(word));
                    }
                    rsp.add("results", results);
                }
            }
        } catch (Exception e) {
            numErrors++;
            LOG.error(e.getMessage());
        } finally {
            totalTime += System.currentTimeMillis() - startTime;
        }
    }


    @Override
    public String getDescription() {
        LOG.debug(getClass() + " @ getDescription(): getDescriptionnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn");
        return "Searchbox DemoPlugin";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String getSource() {
        return "http://www.searchbox.com";
    }

    @Override
    public NamedList<Object> getStatistics() {

        NamedList all = new SimpleOrderedMap<Object>();

        all.add("requests", "" + numRequests);
        all.add("errors", "" + numErrors);
        all.add("totalTime(ms)", "" + totalTime);

        return all;
    }
}
