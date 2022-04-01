package org.apache.jena.query.text;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TestDatsetConcurrencyWithConfigurableAnalyzer extends TestDatasetWithLowerCaseKeywordAnalyzer {
    @Override
    @Before
    public void before () {
        init(StrUtils.strjoinNL(
                "text:ConfigurableAnalyzer ;",
                "text:tokenizer text:WhitespaceTokenizer ;",
                "text:filters (text:ASCIIFoldingFilter text:LowerCaseFilter)"
        ));
        initDataset();
    }

    private void initDataset() {
        Model model = dataset.getDefaultModel();
        dataset.begin(ReadWrite.WRITE);
        for (int i = 0; i < 1000; i++) {
            String token = "Foo" + i;
            model.createResource(RESOURCE_BASE + token).addProperty(RDFS.label, token);
        }
        dataset.commit();
    }

    private boolean testOneQuery(int probe) {
        final String testName = "testConfigurableAnalyzerIsConcurrencySafe" + probe;
        String query = QUERY_PROLOG + "select ?s WHERE {?s text:query (rdfs:label 'foo" + probe + "' 10).}";
        try {
            doTestQuery(dataset, testName, query, Sets.newHashSet(RESOURCE_BASE + "Foo" + probe), 1);
        } catch (Exception e) {
//            e.printStackTrace(System.out);
            return false;
        }
        return true;
    }

    @Test
    public void testConfigurableAnalyzerIsConcurrencySafe () {
        final int parallelism = 8;
        ExecutorService executorService = Executors.newFixedThreadPool(parallelism);
        try {
            for (int i = 0; i < 20; i++) {
                Future<Boolean>[] results = (Future<Boolean>[]) new Future<?>[parallelism];
                for (int j = 0; j < parallelism; j++) {
                    final int probe = i;
                    results[j] = executorService.submit(() -> testOneQuery(probe));
                }
                for (int j = 0; j < parallelism; j++) {
                    Assert.assertTrue("Probe " + i + " failed", results[j].get());
                }
            }
        } catch (InterruptedException e) {
            // exit silently on interrupt
        } catch (ExecutionException e) {
            Assert.assertTrue("Concurrency exception: " + e.getMessage(), false);
        }
    }
}