/*
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

package org.apache.jena.sparql.vocabulary ;

import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.ModelFactory ;
import org.apache.jena.rdf.model.Property ;
import org.apache.jena.rdf.model.Resource ;

public class TestManifest {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();

    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#";

    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}

    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );

    /** <p>Notable feature of this test (advisory)</p> */
    public static final Property notable = m_model.createProperty( "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#notable" );

    /** <p>Required functionality for execution of this test</p> */
    public static final Property requires = m_model.createProperty( "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#requires" );

    /** <p>The expected outcome</p> */
    public static final Property result = m_model.createProperty( "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#result" );

    /** <p>Action to perform</p> */
    public static final Property action = m_model.createProperty( "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#action" );

    /** <p>Optional name of this entry</p> */
    public static final Property name = m_model.createProperty( "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#name" );

    /** <p>Connects the manifest resource to rdf:type list of entries</p> */
    public static final Property entries = m_model.createProperty( "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#entries" );

    /** <p>Connects the manifest resource to rdf:type list of manifests</p> */
    public static final Property include = m_model.createProperty( "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#include" );

    /** <p>Requirements for a particular test</p> */
    public static final Resource Notable = m_model.createResource( "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#Notable" );

    /** <p>Requirements for a particular test</p> */
    public static final Resource Requirement = m_model.createResource( "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#Requirement" );

    /** <p>Statuses a test can have</p> */
    public static final Resource TestStatus = m_model.createResource( "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#TestStatus" );

    /** Assumed base for the tests in the manifest */
    public static final Property assumedTestBase = m_model.createProperty( "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#assumedTestBase" );

    /** <p>The given mf:result for a mf:ReducedCardinalityTest is the results as if the
     *  REDUCED keyword were omitted. To pass a mf:ReducedCardinalityTest, an implementation
     *  must produce a result set with each solution in the expected results appearing
     *  at least once and no more than the number of times it appears in the expected
     *  results. Of course, there must also be no results produced that are not in
     *  the expected results.</p>
     */
    public static final Resource ReducedCardinalityTest = m_model.createResource( "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#ReducedCardinalityTest" );

    /** <p>A type of test specifically for query evaluation testing. Query evaluation
     *  tests are required to have an associated input dataset, a query, and an expected
     *  output dataset.</p>
     */
    public static final Resource QueryEvaluationTest = m_model.createResource( "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#QueryEvaluationTest" );

    /** <p>A type of test specifically for syntax testing. Syntax tests are not required
     *  to have an associated result, only an action. Negative syntax tests are tests
     *  of which the result should be a parser error.</p>
     */
    public static final Resource NegativeSyntaxTest = m_model.createResource( "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#NegativeSyntaxTest" );

    /** <p>A type of test specifically for syntax testing. Syntax tests are not required
     *  to have an associated result, only an action.</p>
     */
    public static final Resource PositiveSyntaxTest = m_model.createResource( "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#PositiveSyntaxTest" );

    /** <p>One entry in rdf:type list of entries</p> */
    public static final Resource ManifestEntry = m_model.createResource( "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#ManifestEntry" );

    /** <p>The class of manifests</p> */
    public static final Resource Manifest = m_model.createResource( "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#Manifest" );

    public static final Resource accepted = m_model.createResource( "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#accepted" );

    /** <p>Tests that require xsd:date operations</p> */
    public static final Resource XsdDateOperations = m_model.createResource( "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#XsdDateOperations" );

    public static final Resource proposed = m_model.createResource( "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#proposed" );

    /** <p>Tests that require language tag handling in FILTERs</p> */
    public static final Resource LangTagAwareness = m_model.createResource( "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#LangTagAwareness" );

    public static final Resource rejected = m_model.createResource( "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#rejected" );

    /** <p>Values in disjoint value spaces are not equal</p> */
    public static final Resource KnownTypesDefault2Neq = m_model.createResource( "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#KnownTypesDefault2Neq" );

    /** <p>Tests that require simple literal is the same value as an xsd:string of the
     *  same lexicial form</p>
     */
    public static final Resource StringSimpleLiteralCmp = m_model.createResource( "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#StringSimpleLiteralCmp" );

    /** <p>Tests that involve lexical forms which are illegal for the datatype</p> */
    public static final Resource IllFormedLiterals = m_model.createResource( "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest#IllFormedLiterals" );

}
