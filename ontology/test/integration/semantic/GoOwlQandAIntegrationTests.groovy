package semantic

import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.OWLClass
import org.semanticweb.owlapi.model.OWLOntology
import org.semanticweb.owlapi.model.OWLOntologyManager

/**
 * Created with IntelliJ IDEA.
 * User: xiaorong
 * Date: 7/6/13
 * Time: 10:27 AM
 * To change this template use File | Settings | File Templates.
 */
class GoOwlQandAIntegrationTests extends GroovyTestCase {
    def iriString = "http://purl.obolibrary.org/obo/GO_0006281"
    def label = "DNA repair"
    OWLOntologyManager manager = OWLManager.createOWLOntologyManager()
    OWLOntology ontology = GoOwlQandA.loadOntoFromFile(manager, null)
    void testGetClassByLabel() throws Exception {
        OWLClass owlClass = GoOwlQandA.getClassByLabel(label, ontology, manager)
        assert owlClass.getIRI().toString() == iriString
    }

    void testGetSubClassOfByLabel() {
        def subclasses = GoOwlQandA.getSubClassOfByLabel(label, ontology, manager)
        assert subclasses.size() == 15 // we are expecting 15 direct subclass of "DNA repair"
        subclasses.each{
            subclass->
            println("${subclass.key} ${subclass.value}")
        }
        def subclasses1 = GoOwlQandA.getSubClassOfByClassName(iriString, ontology, manager)
        assert subclasses1.size() == 15 // we are expecting 15 direct subclass of "DNA repair"
    }

    void testGetSuperClassOfByLabel() {
        def superclasses = GoOwlQandA.getSuperClassOfByLabel(label, ontology, manager)
        assert superclasses.size() == 2
        superclasses.each{
            superclass->
            println("${superclass.key} ${superclass.value}")
        }
        def superclasses1 = GoOwlQandA.getSuperClassOfByClassName(iriString, ontology, manager)
        assert superclasses1.size() == 2
    }

    void testGetSubClassOfByLabelFromReasoner() {
        def reasoner = GoOwlQandA.computeInferred(ontology)
        def subclasses = GoOwlQandA.getSubClassOfByLabelFromReasoner(label, ontology, manager, reasoner, true) // only direct subclass
        assert subclasses.size() == 15 // we are expecting 15 direct subclass of "DNA repair"
        subclasses.each{
            subclass->
            println("${subclass.key} ${subclass.value}")
        }

        subclasses = GoOwlQandA.getSubClassOfByLabelFromReasoner(label, ontology, manager, reasoner, false)
        assert subclasses.size() == 35 // we are expecting total 35 subclass of "DNA repair"
        subclasses.each{
            subclass->
            println("${subclass.key} ${subclass.value}")
        }
    }

    void testProcessSubHierarchies(){
        def reasoner = GoOwlQandA.computeInferred(ontology)
        def clazz = GoOwlQandA.getClassByLabel(label, ontology, manager)
        GoOwlQandA.processSubHierarchies(clazz,reasoner,true)
    }

    void testProcessSuperHierarchies(){
        def reasoner = GoOwlQandA.computeInferred(ontology)
        def clazz = GoOwlQandA.getClassByLabel(label, ontology, manager)
        GoOwlQandA.processSuperHierarchies(clazz,reasoner,true)
    }
}
