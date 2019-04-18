# Karlheinz: GeoServer data management made easy
Karlheinz is a command line tool for easy uploading and publishing of data files to a GeoServer instance, loosely inspired by file synchronization tools like Dropbox or OneDrive. Karlheinz is written in Kotlin and runs on the Java virtual machine.

**Karlheinz is in a very early stage of development! Many planned features are still missing, very few sanity checks are performed, and there are probably still many bugs! USE AT YOUR OWN RISK!**

# How does it work?

When started, Karlheinz scans a user-specified directory on your computer for subfolders and files within these subfolders. For each subfolder, it creates a workspace with the same name on your GeoServer instance. Each file within a subfolder is automatically uploaded to your GeoServer, and, depending on its type, either configured as a data source and published as a layer, or registered as a style. The specific action depends on the file's type, determined by the file name ending:

- A ".shp.zip" file is expected to be a zip archive that contains an ESRI Shapefile dataset (i.e. with .shp, .dbf and other required files present). Its content will be set up as a data source and layer in GeoServer.

- A ".gpkg" file is expected to contain a GeoPackage SQLite geodatabase. Its content will be set up as a data source and layer in GeoServer.

- A ".sld" file is expected to be a Styled Layer Descriptor (SLD) XML document. The SLD document will be installed as a style in your GeoServer instance.

- A ".sld.zip" file is expected to be a zip archive that contains a Style Layer Descriptor (SLD) XML document. The SLD document will be installed as a style in your GeoServer instance.



File types are currently determined by the file ending (sequence of characters after the last occurence of '.' in the file name). 

**CAUTION**: Note that Karlheinz will **overwrite** a data set with the same name if such one exists on the GeoServer instance! 

Also note that Karlheinz will *not* delete a workspace, data set or layer from the GeoServer instance if you delete the corresponding file from your sync dir. Such a "delete mode" might be added as optional behavior in a future version.


## GeoServer behaviour on data source updates

When a data source file is updated (i.e. re-uploaded and overwriting an existing data source file), the respective layer is not re-created from scratch. Existing layer configuration settings are kept. 

## Auto-assignment of layer styles

As an "experimental" feature (yes, even *more* experimental than the whole program itself!), Karlheinz tries to automatically assign an uploaded SLD style as the default style of an existing layer if the following conditions are fulfilled:

- The layer's name equals the style file's base name (file name without extension). For example, the style document "water.sld" would be assigned as the default style of the layer "water".

- The layer's data source resides in the same working space as the style. For global styles, no auto-assignment is performed.

**NOTE: As of 2018-11-26, the layer auto-assignment feature is not available in the current master HEAD version**

# Usage

Karlheinz is a command line tool that comes as a Java .jar runnable archive. You can use it like this:

```
java -jar karlheinz.jar -dir <your-upload-dir> -url <your-geoserver-url> -u <geoserver-user-name> -p <geoserver-password> 
```
Example:

```
java -jar karlheinz.jar -dir /home/me/geoserver_upload -url http://192.168.56.101:8080/geoserver/ -u admin -p topsecret 
```

# Future Development

Future improvements could include:

- Support for configuration and metadata files that allow users to control Karlheinz's behaviour in more detail and upload supplemental data like .sld layer styles and metadata.

- Support for additional file types like GeoJson and CSV, which are not natively supported by GeoServer, by converting them to GeoPackage before upload.

- Adding of a "watch mode" where the program keeps running in the background, monitors changes in the sync directory and uploads changed files automatically

- Adding of a "delete mode" that removes layers, data sources and other items from the GeoServer instance if their corresponding "origin" files in the sync folder were deleted.

- More sanity checks
