# Karlheinz - GeoServer data management made easy!
Karlheinz is a command line tool for easy uploading and publishing of data files to a GeoServer instance, loosely inspired by file synchronization tools like Dropbox or OneDrive. Karlheinz is written in Kotlin and runs on the Java virtual machine.

**Karlheinz is in a very early stage of development! Many planned features are still missing, very few sanity checks are performed, and there are probably still many bugs! USE AT YOUR OWN RISK!**

# What's new?

## 2019-05-25
- This manual: An error in the section about feature type XML definitions has been corrected.
- This manual: Missing information about the handling of data store XML definitions has been added.
- A compiled, ready-to-use karlheinz.jar has been added to the repository.

## 2019-05-14

A large part of Karlheinz was heavily redesigned in order to make the code shorter and simpler. This also brought some changes in the program's behaviour. This manual has been updated accordingly. Also, a first set of unit tests were added.

## 2019-05-11
By default, Karlheinz does no longer overwrite existing resources on a GeoServer instance. See section "Overwriting and deleting of existing resources" for more information.

## 2019-05-10
Karlheinz now supports the creation of layers from existing multi-layer data stores like PostgreSQL/PostGIS connections or GeoPackage SQLite files. 

# How does it work?

## Workspaces

When started, Karlheinz scans a user-specified directory on your computer for subfolders and files within these subfolders. For each subfolder, it creates a workspace with the same name on your GeoServer instance. 

## Data stores

Karlheinz iterates over all files and folders within a workspace folder and performs different operations with each item, depending on its file ending.

- A ".zip" file is expected to be a zip archive that contains an ESRI Shapefile dataset (i.e. with .shp, .dbf and other required files present). It will be uploaded and its content will be set up as a data source and feature type (layer) in GeoServer. 

- A ".gpkg" file is expected to contain a GeoPackage SQLite geodatabase. Its content will be set up as a data source and layer in GeoServer. Note that for GeoPackage files that contain multiple feature types, GeoServer will set up only one of them as a GeoServer layer automatically. This is a bug/limitation in GeoServer. As a workaround, you can provide an XML feature type description file for each other layer in the file.

- A ".xml" file is expected to be a GeoServer data store XML description. For example, these can be PostGIS database connections or references to .shp or .gpkg files which were not uploaded by Karlheinz and/or are located in other folders than the GeoServer data directory. Take a look at the GeoServer REST API documentation to learn about the expected structure of a GeoServer data store XML definition.

## Feature types

Feature types (or layers) are references to individual data sets (layers/tables/etc.) in a data store that supports multiple layers, like GeoPackage or PostGIS. When a Shapefile or GeoPackage data store file is uploaded (see section "Data Stores"), GeoServer automatically creates a feature type entry for *one* of the layers contained in the uploaded data store file. Shapefiles can contain only one layer anyway, but GeoPackage files can contain multiple layers for which you need to set up a GeoServer feature type separately. The same applies to other multi-layer data stores like PostGIS connections.

In order to publish a layer from a GeoServer data store through Karlheinz, you first need to create a subfolder within the workspace folder where the data store is located. The subfolder needs to be given the same name as the data store. For example, if you have a multi-layer GeoPackage file named "test.gpkg" in your workspace folder and want to publish layers from that GeoPackage, the subfolder must be named "test". Then, for each layer you want to publish, put a .XML file containing the respective GeoServer XML feature type definition into the subfolder. Take a look at the GeoServer REST API documentation to learn about the expected structure of a GeoServer feature type XML definition.

## Styles

If a workspace directory contains a subfolder named "styles", Karlheinz will upload the contained files to GeoServer and register them as workspace-level styles.

- A ".sld" file in the "styles" subfolder is expected to be a Styled Layer Descriptor (SLD) XML document. 

- A ".zip" file in the "styles" subfolder is expected to be a zip archive that contains a Style Layer Descriptor (SLD) XML document.

### Automatic style assignment

Karlheinz can automatically assign an uploaded workspace-level SLD style as the default style of an existing layer in the same workspace if the following conditions are fulfilled:

- The layer's name equals the style file's base name (file name without extension). For example, the style document "water.sld" would be assigned as the default style of the layer "water". The same applies to an uploaded style file named "water.zip".

- The layer's data source resides in the same workspace as the style. For global styles, no auto-assignment is performed.


## A general comment about file types

File types are currently determined by the file ending (sequence of characters after the last occurence of '.' in the file name). 

## Overwriting and deleting of existing resources
By default, Karlheinz does not overwrite existing data stores and styles. 

- To force overwriting of existing data stores, add the command line flag '-od'. 

- To force overwriting of existing feature types, add the command line flag '-of'.

- To force overwriting of existing styles, add the command line flag '-os'.

Karlheinz will not delete a workspace, data set or layer from the GeoServer instance if you delete the corresponding file from your upload dir. Such a "delete mode" might be added as optional behavior in a future version.

# Usage

## Basic usage

Karlheinz is a command line tool that comes as a Java .jar runnable archive. You can use it like this:

```
java -jar karlheinz.jar -dir <your-upload-dir> -url <your-geoserver-url> -u <geoserver-user-name> -p <geoserver-password> [-od] [-of] [-os]
```
Example:

```
java -jar karlheinz.jar -dir /home/me/geoserver_upload -url http://192.168.56.101:8080/geoserver/ -u admin -p topsecret -od -of -os
```

Omitting the optional '-od', '-of' and '-os' flags disables overwriting for *d*data stores, *f*eature types or *s*tyles, respectively (also see section "Overwriting and deleting of existing resources" above).

## Creating layers from existing data stores

To create a layer from an existing data store, 

1. create a folder named "<your-datastore-name>" inside a workspace folder. The workspace should contain a data store with the respective name. This could be a GeoPackage file in the same folder which is uploaded by Karlheinz, a PostGIS connection, or any other type of GeoServer data store.
  
2. Within that folder, for each layer that you want to create, place an .xml file with an XML document that conforms to the following (minimal) scheme:

```
<featureType>
  <name>name-of-the-database-table-to-publish</name>
  <nativeName>name-of-the-database-table-to-publish</nativeName>
</featureType>
```

Additional XML elements can be added according to the GeoServer feature type XML schema.


# Future Development

Future improvements could include:

- Support for configuration and metadata files that allow users to control Karlheinz's behaviour in more detail and upload supplemental data like .sld layer styles and metadata.

- Support for additional file types like GeoJson and CSV, which are not natively supported by GeoServer, by converting them to GeoPackage before upload.

- Adding of a "watch mode" where the program keeps running in the background, monitors changes in the upload directory and uploads changed files automatically

- Adding of a "delete mode" that removes layers, data sources and other items from the GeoServer instance if their corresponding "origin" files in the upload folder were deleted.

- More sanity checks
