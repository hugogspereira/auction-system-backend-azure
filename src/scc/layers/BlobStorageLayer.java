package scc.layers;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static scc.utils.AzureProperties.BLOB_KEY;

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
            String storageConnectionString = System.getenv(BLOB_KEY);
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
            if(!blob.exists()) {
                blob.upload(BinaryData.fromBytes(data));
            }
            /*
            DEBUG:
            System.out.println( "File uploaded : " + filename);
            */
        }

        public byte[] download(String photoId) {
            // Get client to blob
            BlobClient blob = blobContainerClient.getBlobClient(photoId);
            // Download contents to BinaryData (check documentation for other alternatives)
            if(!blob.exists()) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
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

        public List<String> list(){
            List<String> list = new ArrayList<>();
            for (BlobItem image : blobContainerClient.listBlobs()) {
                list.add(image.getName());
            }
            return list;
        }
    }