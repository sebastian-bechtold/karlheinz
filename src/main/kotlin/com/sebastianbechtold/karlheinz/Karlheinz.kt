
import com.sebastianbechtold.geoserverrestclient.GeoServerRestClient
import com.sebastianbechtold.geoserversync.GeoServerSync
import com.sebastianbechtold.nanohttp.httpRequest
import org.kohsuke.args4j.Argument
import org.kohsuke.args4j.Option
import org.kohsuke.args4j.CmdLineException
import org.kohsuke.args4j.CmdLineParser
import org.kohsuke.args4j.OptionHandlerFilter.ALL
import java.util.ArrayList
import java.io.File

class Args {

    @Option(name = "-dir", required = true, usage = "Directory to upload")
    var syncDir: String = ""

    @Option(name = "-url", required = true, usage = "GeoServer URL")
    var geoServerUrl: String = ""

    @Option(name = "-p", required = true, usage = "GeoServer password")
    var password: String = ""

    @Option(name = "-u", required = true, usage = "GeoServer username")
    var username: String = ""

    @Option(name = "-od", required = false, usage = "Overwrite data stores?")
    var overwriteDataStores: Boolean = false

    @Option(name = "-os", required = false, usage = "Overwrite styles?")
    var overwriteStyles: Boolean = true


    // receives other command line parameters than options
    @Argument
    private var arguments = ArrayList<String>()

    fun init(args : Array<String>) {

        val parser = CmdLineParser(this)

        try {
            // parse the arguments.
            parser.parseArgument(*args)

            // you can parse additional arguments if you want.
            // parser.parseArgument("more","args");

            // after parsing arguments, you should check
            // if enough arguments are given.
            /*
            if (arguments.isEmpty()) {
                throw CmdLineException(parser, "Missing command line arguments")
            }
            */

        } catch (e: CmdLineException) {
            // if there's a problem in the command line,
            // you'll get this exception. this will report
            // an error message.
            System.err.println("Command line error: " + e.message)
           // System.err.println("java SampleMain [options...] arguments...")
            // print the list of available options
            parser.printUsage(System.err)
            System.err.println()

            // print option sample. This is useful some time
            System.err.println("  Example: " + parser.printExample(ALL))

            return
        }
    }
}


fun main(arguments : Array<String>) {

    var args = Args()
    args.init(arguments)


    var syncer = GeoServerSync(GeoServerRestClient(args.geoServerUrl, args.username, args.password), args.overwriteDataStores, args.overwriteStyles)

    println("Starting upload to GeoServer at " + args.geoServerUrl)

    var result = syncer.syncDir(File(args.syncDir))

    if (!result) {
        println("ERROR: Invalid upload directory")
    }
    else {
        println("Complete.")
    }
}
