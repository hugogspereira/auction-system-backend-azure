package scc.layers;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import javax.ws.rs.WebApplicationException;
import java.util.List;

public class BlobStorageLayer {
    private static BlobStorageLayer instance;
    private static BlobContainerClient blobContainerClient;

    public BlobStorageLayer(BlobContainerClient blobContainerClient) {
        this.blobContainerClient = blobContainerClient;
    }

    public static synchronized BlobStorageLayer getInstance() {
            if(instance != null) {
                return instance;
            }
            // Get connection string in the storage access keys page
            String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName=scc2122storage;AccountKey=???/???+???+???+???/jg==;EndpointSuffix=core.windows.net";
            // Get container client
            BlobContainerClient blobContainerClient = new BlobContainerClientBuilder()
                                                        .connectionString(storageConnectionString)
                                                        .containerName("images")
                                                        .buildClient();

            instance = new BlobStorageLayer(blobContainerClient);
            return instance;
        }

        public void upload(String photoId, byte[] data) {
            // Get client to blob
            BlobClient blob = blobContainerClient.getBlobClient(photoId);
            // Upload contents from BinaryData (check documentation for other alternatives)
            blob.upload(BinaryData.fromBytes(data));
            /*
            DEBUG:
            System.out.println( "File uploaded : " + filename);
            */
        }

        public byte[] download(String photoId) {
            // Get client to blob
            BlobClient blob = blobContainerClient.getBlobClient(photoId);
            // Download contents to BinaryData (check documentation for other alternatives)
            BinaryData data = blob.downloadContent();
            /*
            DEBUG:
            byte[] arr = data.toBytes();
            System.out.println( "Blob size : " + arr.length);
            */

            return data.toBytes();
        }

        public boolean existsBlob(String photoId) {
            BlobClient blob = blobContainerClient.getBlobClient(photoId);
            /*
            DEBUG:
            System.out.println( "Blob exists : " + blob.exists());
            */
            return blob.exists();
        }
    }