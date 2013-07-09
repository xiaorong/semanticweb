package semantic;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: xiaorong
 * Date: 7/5/13
 * Time: 8:21 PM
 * To change this template use File | Settings | File Templates.
 * <p/>
 * This class provide functions to query GO ontology in owl format. More functions can be added when need.
 * The library I am using is http://owlapi.sourceforge.net/
 */
public class GoOwlQandA {
    // this url is resolvable
    public static String GO_ONTOLOGY_WEB = "http://www.berkeleybop.org/ontologies/go.owl";

    /**
     * Load ontology from web given a resolvable ontologyURI
     *
     * @param manager
     * @param ontologyURI
     * @return OWLOntology
     * @throws Exception
     */
    public static OWLOntology loadOntoFromWeb(OWLOntologyManager manager, String ontologyURI) throws Exception {
        if (manager == null) {
            manager = OWLManager.createOWLOntologyManager();
        }
        // load ontology from web if the URI is resolvable
        if (ontologyURI == null) {
            ontologyURI = GO_ONTOLOGY_WEB;
        }
        IRI iri = IRI.create(ontologyURI);
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(iri);
        // We can always obtain the location where an ontology was loaded from
        IRI documentIRI = manager.getOntologyDocumentIRI(ontology);
        System.out.println("    from: " + documentIRI);
        return ontology;
    }

    /**
     * Load ontology from a local file
     *
     * @param manager
     * @param fileName
     * @return OWLOntology
     * @throws Exception
     */
    public static OWLOntology loadOntoFromFile(OWLOntologyManager manager, String fileName) throws Exception {
        if (fileName == null)
            fileName = "data/go.owl";
        File file = new File(fileName);

        if (manager == null) {
            manager = OWLManager.createOWLOntologyManager();
        }
        // Now load the local copy
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);
        System.out.println("Loaded ontology from : " + fileName + ontology);
        // We can always obtain the location where an ontology was loaded from
        IRI documentIRI = manager.getOntologyDocumentIRI(ontology);
        System.out.println("    from: " + documentIRI);
        return ontology;
    }

    /**
     * Get all direct subclasses of a class given by the label of the class
     *
     * @param label
     * @param ontology
     * @param manager
     * @return a map with term IRI as key, label as value
     */
    public static Map<String, String> getSubClassOfByLabel(String label, OWLOntology ontology, OWLOntologyManager manager) {

        OWLDataFactory owlDataFactory = manager.getOWLDataFactory();
        OWLClass owlClass = getClassByLabel(label, ontology, manager);
        Map<String, String> subClassesMap = getNamedSubClassOfClass(ontology, owlDataFactory, owlClass);
        return subClassesMap;
    }

    /**
     * Get all direct subclasses of a class given by the the class name (IRI)
     *
     * @param className
     * @param ontology
     * @param manager
     * @return a map with term IRI as key, label as value
     */
    public static Map<String, String> getSubClassOfByClassName(String className, OWLOntology ontology, OWLOntologyManager manager) {

        OWLDataFactory owlDataFactory = manager.getOWLDataFactory();
        OWLClass owlClass = owlDataFactory.getOWLClass(IRI.create(className));
        Map<String, String> subClassesMap = getNamedSubClassOfClass(ontology, owlDataFactory, owlClass);
        return subClassesMap;
    }

    /**
     * Get all direct superclasses of a class given the label of the class
     *
     * @param label
     * @param ontology
     * @param manager
     * @return a map with term IRI as key, label as value
     */
    public static Map<String, String> getSuperClassOfByLabel(String label, OWLOntology ontology, OWLOntologyManager manager) {

        OWLDataFactory owlDataFactory = manager.getOWLDataFactory();
        OWLClass owlClass = getClassByLabel(label, ontology, manager);
        Map<String, String> subClassesMap = getNamedSuperClassOfClass(ontology, owlDataFactory, owlClass);
        return subClassesMap;
    }

    /**
     * Get all direct superclasses of a class given the class name (IRI)
     *
     * @param className
     * @param ontology
     * @param manager
     * @return a map with term IRI as key, label as value
     */
    public static Map<String, String> getSuperClassOfByClassName(String className, OWLOntology ontology, OWLOntologyManager manager) {

        OWLDataFactory owlDataFactory = manager.getOWLDataFactory();
        OWLClass owlClass = owlDataFactory.getOWLClass(IRI.create(className));
        Map<String, String> subClassesMap = getNamedSuperClassOfClass(ontology, owlDataFactory, owlClass);
        return subClassesMap;
    }


    private static Map<String, String> getNamedSubClassOfClass(OWLOntology ontology, OWLDataFactory owlDataFactory, OWLClass owlClass) {
        Set<OWLClassExpression> subClasses = owlClass.getSubClasses(ontology);
        return getNamedClassLabelMap(ontology, owlDataFactory, subClasses);
    }

    private static Map<String, String> getNamedSuperClassOfClass(OWLOntology ontology, OWLDataFactory owlDataFactory, OWLClass owlClass) {
        Set<OWLClassExpression> subClasses = owlClass.getSuperClasses(ontology);
        return getNamedClassLabelMap(ontology, owlDataFactory, subClasses);
    }

    private static Map<String, String> getNamedClassLabelMap(OWLOntology ontology, OWLDataFactory owlDataFactory, Set<OWLClassExpression> classes) {
        OWLAnnotationProperty rdfsLabel = owlDataFactory.getRDFSLabel();
        Map<String, String> classLabelMap = new HashMap<String, String>();
        for (OWLClassExpression subClass : classes) {
            if (subClass.getClassExpressionType() == ClassExpressionType.OWL_CLASS) {
                OWLClass owlClass1 = subClass.asOWLClass();
                classLabelMap.put(owlClass1.getIRI().toString(), getLabelFromClass(owlClass1, ontology, rdfsLabel));
            }
        }
        return classLabelMap;
    }

    private static String getLabelFromClass(OWLClass owlClass, OWLOntology ontology, OWLAnnotationProperty rdfsLabel) {
        // Get the annotations on the class that use the label property, this class might be annotated with multiple labels
        // but we only expect one
        for (OWLAnnotation annotation : owlClass.getAnnotations(ontology, rdfsLabel)) {
            if (annotation.getValue() instanceof OWLLiteral) {
                OWLLiteral val = (OWLLiteral) annotation.getValue();
                return val.getLiteral();
            }
        }
        return null;
    }

    /**
     * Given a label, return a OWLClass, one OWLClass might have multiple labels.
     *
     * @param label
     * @param ontology
     * @param manager
     * @return OWLClass
     */
    public static OWLClass getClassByLabel(String label, OWLOntology ontology, OWLOntologyManager manager) {
        Set<OWLClass> classesInSignature = ontology.getClassesInSignature();
        OWLDataFactory owlDataFactory = manager.getOWLDataFactory();
        OWLAnnotationProperty rdfsLabel = owlDataFactory.getRDFSLabel();
        for (OWLClass owlClass : classesInSignature) {
            String foundLabel = getLabelFromClass(owlClass, ontology, rdfsLabel);
            if (label.equals(foundLabel))
                return owlClass;
        }
        return null;
    }

    /**
     * Get all direct or indirect flattened subclasses of a class given its label.
     *
     * @param label
     * @param ontology
     * @param manager
     * @param reasoner
     * @param direct
     * @return a map with term IRI as key, label as value
     */
    public static Map<String, String> getSubClassOfByLabelFromReasoner(String label, OWLOntology ontology, OWLOntologyManager manager, OWLReasoner reasoner, boolean direct) {

        OWLDataFactory owlDataFactory = manager.getOWLDataFactory();
        OWLClass owlClass = getClassByLabel(label, ontology, manager);
        NodeSet<OWLClass> subClses = reasoner.getSubClasses(owlClass, direct);
        Set<OWLClass> clses = subClses.getFlattened();
        OWLAnnotationProperty rdfsLabel = owlDataFactory.getRDFSLabel();
        Map<String, String> classLabelMap = new HashMap<String, String>();
        for (OWLClass subClass : clses) {
            if (!subClass.isBottomEntity())   // don't want http://www.w3.org/2002/07/owl#Nothing
                classLabelMap.put(subClass.getIRI().toString(), getLabelFromClass(subClass, ontology, rdfsLabel));
        }
        return classLabelMap;
    }

    /**
     * Process node recursively
     *
     * @param owlClass
     * @param reasoner
     * @param direct
     */
    public static void processSubHierarchies(OWLClass owlClass, OWLReasoner reasoner, boolean direct) {
        NodeSet<OWLClass> subClses = reasoner.getSubClasses(owlClass, direct);
        for (Node<OWLClass> node : subClses.getNodes()) {
            Set<OWLClass> entities = node.getEntities();
            if (node.isBottomNode() && entities.size() != 1) { // not only have OWL:Nothing as bottom node
                for (OWLClass entity : entities) {
                    System.out.println(entity.getIRI().toString());
                }
                continue;           // it possibly has multiple bottom nodes, so continue
            } else {
                System.out.println(owlClass.getIRI().toString() + ":" + entities);    // print current node and its subclasses
            }
            for (OWLClass entity : entities) {
                processSubHierarchies(entity, reasoner, direct);
            }
        }
    }

    /**
     * Process node recursively
     *
     * @param owlClass
     * @param reasoner
     * @param direct
     */
    public static void processSuperHierarchies(OWLClass owlClass, OWLReasoner reasoner, boolean direct) {
        NodeSet<OWLClass> superClasses = reasoner.getSuperClasses(owlClass, direct);
        for (Node<OWLClass> node : superClasses.getNodes()) {
            Set<OWLClass> entities = node.getEntities();
            if (node.isTopNode() && entities.size() != 1) { // not only have OWL:Nothing as top node
                for (OWLClass entity : entities) {
                    System.out.println(entity.getIRI().toString());
                }
                continue;           // it possibly has multiple top nodes, so continue
            } else {
                System.out.println(owlClass.getIRI().toString() + ":" + entities);    // print current node and its super classes
            }
            for (OWLClass entity : entities) {
                processSuperHierarchies(entity, reasoner, direct);
            }
        }
    }

    /**
     * Compute inferred fact from a ontology
     *
     * @param ontology
     * @return OWLReasoner
     */
    public static OWLReasoner computeInferred(OWLOntology ontology) {
        // Use HermiT Reasoner
        OWLReasonerFactory reasonerFactory = new org.semanticweb.HermiT.Reasoner.ReasonerFactory();
        ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
        OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
        OWLReasoner reasoner = reasonerFactory.createReasoner(ontology, config);

        reasoner.precomputeInferences();
        boolean consistent = reasoner.isConsistent();
        System.out.println("Consistent: " + consistent);
        System.out.println("\n");
        System.out.println("reasonerName:" + reasoner.getReasonerName());

        // We can easily get a list of unsatisfiable classes.  (A class is unsatisfiable if it
        // can't possibly have any instances).  Note that the getUnsatisfiableClasses method
        // is really just a convenience method for obtaining the classes that are equivalent
        // to owl:Nothing.  In our case there should be just one unsatisfiable class - "mad_cow"
        // We ask the reasoner for the unsatisfiable classes, which returns the bottom node
        // in the class hierarchy (an unsatisfiable class is a subclass of every class).
        Node<OWLClass> bottomNode = reasoner.getUnsatisfiableClasses();
        // This node contains owl:Nothing and all the classes that are equivalent to owl:Nothing -
        // i.e. the unsatisfiable classes.
        // We just want to print out the unsatisfiable classes excluding owl:Nothing, and we can
        // used a convenience method on the node to get these
        Set<OWLClass> unsatisfiable = bottomNode.getEntitiesMinusBottom();
        if (!unsatisfiable.isEmpty()) {
            System.out.println("The following classes are unsatisfiable: ");
            for (OWLClass cls : unsatisfiable) {
                System.out.println("    " + cls);
            }
        } else {
            System.out.println("There are no unsatisfiable classes");
        }
        System.out.println("\n");
        return reasoner;
    }

}
