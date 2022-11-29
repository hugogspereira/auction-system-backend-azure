package scc.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AzureProperties
{
	public static final String BLOB_KEY = "BlobStoreConnection";
	public static final String COSMOSDB_KEY = "COSMOSDB_KEY";
	public static final String COSMOSDB_URL = "COSMOSDB_URL";
	public static final String COSMOSDB_DATABASE = "COSMOSDB_DATABASE";
	public static final String MONGODB_HOSTNAME = "MONGODB_HOSTNAME";
	public static final String MONGODB_DATABASE = "MONGODB_DATABASE";
	public static final String REDIS_KEY = "REDIS_KEY";
	public static final String REDIS_URL = "REDIS_URL";
	public static final String COGNITIVESEARCH_QUERYKEY = "COGNITIVESEARCH_QUERYKEY";
	public static final String COGNITIVESEARCH_URL = "COGNITIVESEARCH_URL";
	public static final String COGNITIVESEARCH_INDEX = "COGNITIVESEARCH_INDEX";

	public static final String PROPS_FILE = "azurekeys-westeurope.props";
	private static Properties props;
	
	public static synchronized Properties getProperties() {
		if( props == null) {
			props = new Properties();
			try {
				props.load( new FileInputStream(PROPS_FILE));
			} catch (IOException e) {
				// do nothing
			}
		}
		return props;
	}

}
