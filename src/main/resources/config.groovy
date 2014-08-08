import org.codehaus.groovy.control.customizers.ImportCustomizer
/**
 * Created by samueldoyle on 6/29/13.
 */

withConfig(configuration) {
    source(basename: 'vcoSimpleController*') {
        new ImportCustomizer().addImports('import groovy.util.logging.Slf4j')
        ast(groovy.util.logging.Slf4j)
    }
}
