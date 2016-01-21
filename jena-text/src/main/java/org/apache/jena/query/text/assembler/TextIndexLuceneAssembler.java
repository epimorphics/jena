/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.query.text.assembler ;

import static org.apache.jena.query.text.assembler.TextVocab.pAnalyzer;
import static org.apache.jena.query.text.assembler.TextVocab.pDirectory;
import static org.apache.jena.query.text.assembler.TextVocab.pEntityMap;
import static org.apache.jena.query.text.assembler.TextVocab.pMultilingualSupport;
import static org.apache.jena.query.text.assembler.TextVocab.pQueryAnalyzer;
import static org.apache.jena.query.text.assembler.TextVocab.pStoreValues;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.Mode;
import org.apache.jena.assembler.assemblers.AssemblerBase;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.query.text.EntityDefinition;
import org.apache.jena.query.text.TextDatasetFactory;
import org.apache.jena.query.text.TextIndex;
import org.apache.jena.query.text.TextIndexConfig;
import org.apache.jena.query.text.TextIndexException;
import org.apache.jena.query.text.TextIndexLucene;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.util.graph.GraphUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

public class TextIndexLuceneAssembler extends AssemblerBase {
    /*
    <#index> a :TextIndexLucene ;
        #text:directory "mem" ;
        #text:directory "DIR" ;
        text:directory <file:DIR> ;
        text:entityMap <#endMap> ;
        .
    */
	
	private static RAMDirectoryMap ramDirectories = new RAMDirectoryMap();;
    
    @Override
    public TextIndex open(Assembler a, Resource root, Mode mode) {
        try {
            if ( !GraphUtils.exactlyOneProperty(root, pDirectory) )
                throw new TextIndexException("No 'text:directory' property on " + root) ;

            Directory directory ;
            
            RDFNode n = root.getProperty(pDirectory).getObject() ;
            if ( n.isLiteral() ) {
                String literalValue = n.asLiteral().getLexicalForm() ; 
                if (literalValue.equals("mem")) {
                	// on RAMDirectory per node, not one per invocation
                	directory = ramDirectories.get(root);
                	if (directory == null) {
                        directory = new RAMDirectory() ;
                        ramDirectories.put(root, (RAMDirectory) directory);
                	}
                } else {
                    File dir = new File(literalValue) ;
                    directory = FSDirectory.open(dir) ;
                }
            } else {
                Resource x = n.asResource() ;
                String path = IRILib.IRIToFilename(x.getURI()) ;
                File dir = new File(path) ;
                directory = FSDirectory.open(dir) ;
            }

            Analyzer analyzer = null;
            Statement analyzerStatement = root.getProperty(pAnalyzer);
            if (null != analyzerStatement) {
                RDFNode aNode = analyzerStatement.getObject();
                if (! aNode.isResource()) {
                    throw new TextIndexException("Text analyzer property is not a resource : " + aNode);
                }
                Resource analyzerResource = (Resource) aNode;
                analyzer = (Analyzer) a.open(analyzerResource);
            }

            Analyzer queryAnalyzer = null;
            Statement queryAnalyzerStatement = root.getProperty(pQueryAnalyzer);
            if (null != queryAnalyzerStatement) {
                RDFNode qaNode = queryAnalyzerStatement.getObject();
                if (! qaNode.isResource()) {
                    throw new TextIndexException("Text query analyzer property is not a resource : " + qaNode);
                }
                Resource analyzerResource = (Resource) qaNode;
                queryAnalyzer = (Analyzer) a.open(analyzerResource);
            }

            boolean isMultilingualSupport = false;
            Statement mlSupportStatement = root.getProperty(pMultilingualSupport);
            if (null != mlSupportStatement) {
                RDFNode mlsNode = mlSupportStatement.getObject();
                if (! mlsNode.isLiteral()) {
                    throw new TextIndexException("text:multilingualSupport property must be a string : " + mlsNode);
                }
                isMultilingualSupport = mlsNode.asLiteral().getBoolean();
            }

            boolean storeValues = false;
            Statement storeValuesStatement = root.getProperty(pStoreValues);
            if (null != storeValuesStatement) {
                RDFNode svNode = storeValuesStatement.getObject();
                if (! svNode.isLiteral()) {
                    throw new TextIndexException("text:storeValues property must be a string : " + svNode);
                }
                storeValues = svNode.asLiteral().getBoolean();
            }

            Resource r = GraphUtils.getResourceValue(root, pEntityMap) ;
            EntityDefinition docDef = (EntityDefinition)a.open(r) ;
            TextIndexConfig config = new TextIndexConfig(docDef);
            config.setAnalyzer(analyzer);
            config.setQueryAnalyzer(queryAnalyzer);
            config.setMultilingualSupport(isMultilingualSupport);
            config.setValueStored(storeValues);

            TextIndexLucene index = TextDatasetFactory.createLuceneIndex(directory, config) ;
            index.addEventHandler(TextIndexLucene.Event.CLOSED, 
            		( i ) -> ramDirectories.remove(root));
            return index;
        } catch (IOException e) {
            IO.exception(e) ;
            return null ;
        }
    }
protected static class RAMDirectoryMap {
    	
    	private static WeakHashMap<RDFNode, WeakReference<RAMDirectory>> map = new WeakHashMap<RDFNode, WeakReference<RAMDirectory>>();

    	protected synchronized RAMDirectory get(RDFNode node) {
    		WeakReference<RAMDirectory> ref = map.get(node);
    		if (ref == null)
    			return null;
    		RAMDirectory result = ref.get();
    		if (result == null)
    			map.put(node, null);
    		return result;
    	}
    	
    	protected synchronized void put(RDFNode node, RAMDirectory directory) {
    		map.put(node, new WeakReference<RAMDirectory>(directory));
    	}
    	
    	protected synchronized void remove(RDFNode node) {
    		map.put(node, null);
    	}
    }
}
