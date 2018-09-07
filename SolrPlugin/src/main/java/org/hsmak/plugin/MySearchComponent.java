package org.hsmak.plugin;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.DocList;
import org.apache.solr.search.SolrIndexSearcher;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * Created by hsmak on 12/1/16.
 */
public class MySearchComponent extends SearchComponent {

    private static Logger LOG = Logger.getLogger(SearchComponent.class);

    /**
     * Statistics variables
     */
    volatile long numRequests;
    volatile long numErrors;
    volatile long totalRequestsTime;
    volatile String lastnewSearcher;
    volatile String lastOptimizeEvent;

    protected String defaultField;
    private List<String> words;

    /**
     * This isn't abstract method in the parent class. I had to override it manually according to one tutorial.
     *
     * @param args - this parameter is populated from solrconfig.xml during solr startup
     *             <p>
     *             Initialization is called when the plugin is first loaded
     *             This most commonly occurs when Solr is started up
     *             At this point we can load things from file (models, serialized objects, etc)
     *             Have access to the variables set in solrconfig.xml
     */
    @Override
    public void init(NamedList args) {

        LOG.debug(getClass() + " @ init(): inittttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt");

        super.init(args);

        defaultField = (String) args.get("field");
        if (defaultField == null) {
            throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Need to specify the default for analysis");
        }

        words = ((NamedList) args.get("words")).getAll("word");
        if (words.isEmpty()) {
            throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Need to specify at least one word in searchComponent config!");
        }
    }

    @Override
    public void prepare(ResponseBuilder responseBuilder) throws IOException {
        LOG.debug(getClass() + " @ Prepare(): prepareeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
    }

    @Override
    public void process(ResponseBuilder responseBuilder) throws IOException {

        LOG.debug(getClass() + " @ Process(): processssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss");


        /**************************
         * Do something useful - 1
         **************************/


        /*
         * We start off by keeping track in a volatile variable the number of requests we’ve seen (for use later in statistics), and
         * we’d like to know how long the process takes so we note the time.
         */
        numRequests++;

        SolrParams params = responseBuilder.req.getParams();
        long lstartTime = System.currentTimeMillis();
        SolrIndexSearcher searcher = responseBuilder.req.getSearcher();


        /*
         * We create a new NamedList which will hold this components response
         */
        NamedList response = new SimpleOrderedMap();



        /*
         * We look at the URL parameters to see if there is a “field” variable present.
         * We have set this up to override  the default we loaded from the config file
         */
        String queryField = params.get("field");
        String field = null;

        if (defaultField != null) {
            field = defaultField;
        }

        if (queryField != null) {
            field = queryField;
        }

        if (field == null) {
            LOG.error("Fields aren't defined, not performing counting.");
            return;
        }


        /**************************
         * Do something useful - 2
         **************************/


        /*
         * Since the search has already been completed,
         * we get a list of documents which will be returned.
         */
        DocList docs = responseBuilder.getResults().docList;
        if (docs == null || docs.size() == 0) {
            LOG.debug("No results");
        }
        LOG.debug("Doing This many docs:\t" + docs.size());

        Set<String> fieldSet = new HashSet<String>();


        /*
         * We also need to pull from the schema the field which contains the unique id.
         * This will let us correlate our results with the rest of the response
         */
        //getSchema() is not resolved <- probably due to old Solr version
//        SchemaField keyField = responseBuilder.req.getCore().getSchema().getUniqueKeyField();
        SchemaField keyField = responseBuilder.req.getSchema().getUniqueKeyField();
        if (null != keyField) {
            fieldSet.add(keyField.getName());
        }

        fieldSet.add(field);


        /**************************
         * Do something useful - 3
         **************************/


        //Get a document iterator to look through all docs
        DocIterator iterator = docs.iterator();
        for (int i = 0; i < docs.size(); i++) {

            try {

                int docId = iterator.nextDoc();

                //Setup count variable this doc
                HashMap<String, Double> counts = new HashMap<String, Double>();

                //Load the document through the searcher
                Document doc = searcher.doc(docId, fieldSet);

                //Get the value of the field
                IndexableField[] multifield = doc.getFields(field);
                for (IndexableField singlefield : multifield) {//BEWARE if it is a multifield, using getField will only return the first instance, not ALL instances
                    for (String string : singlefield.stringValue().split(" ")) {
                        if (words.contains(string)) {
                            Double oldcount = counts.containsKey(string) ? counts.get(string) : 0.0;
                            counts.put(string, oldcount + 1);//Do our basic word counting
                        }
                    }
                }

                //Get the document unique id from the keyfield
                String id = doc.getField(keyField.getName()).stringValue();

                NamedList<Double> docresults = new NamedList<Double>();
                for (String word : words) {
                    //Add each word to the results for the doc
                    docresults.add(word, counts.get(word));
                }

                //Add the doc result to the overall response, using its id value
                response.add(id, docresults);
            } catch (IOException ex) {

                LOG.debug(Level.SEVERE, ex);
            }
        }

        /*
         * Add all results to the final response.
         * The name we pick here will show up in the Solr output
         */
        responseBuilder.rsp.add("demoSearchComponent", response);

        //Note down how long it took for the entire process
        totalRequestsTime += System.currentTimeMillis() - lstartTime;

    }

    /****************************************
     * More non-abstract overridden methods
     ****************************************/
    @Override
    public String getDescription() {
        LOG.debug(getClass() + " @ getDescription(): getDescriptionnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn");
        return "Searchbox DemoSearchComponent";
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
        all.add("totalTime(ms)", "" + totalRequestsTime);

        return all;
    }

}
