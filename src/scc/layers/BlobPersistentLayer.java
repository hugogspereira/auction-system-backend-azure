package scc.layers;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BlobPersistentLayer {

    private static BlobPersistentLayer instance;
    private static final String persistentVolume = "/mnt/vol/";

    public BlobPersistentLayer() {
    }

    public static synchronized BlobPersistentLayer getInstance() {
            if(instance != null) {
                return instance;
            }
            instance = new BlobPersistentLayer();
            return instance;
        }

        public void upload(String photoId, byte[] data) {
            try {
                File persistentFile = new File(persistentVolume+photoId);

                FileOutputStream outputStream = new FileOutputStream(persistentFile);
                outputStream.write(data);
                outputStream.close();
            }
            catch (Exception e) {
                throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
            }
            /*
            BlobClient blob = blobContainerClient.getBlobClient(photoId);
            if(!blob.exists()) { blob.upload(BinaryData.fromBytes(data)); }
            */
        }

        public byte[] download(String photoId) {
            File persistentFile = new File(persistentVolume+photoId);
            if(!persistentFile.isFile()) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }

            try {
                FileInputStream fileInputStream = new FileInputStream(persistentFile);
                byte[] data = fileInputStream.readAllBytes();
                fileInputStream.close();
                return data;
            }
            catch (Exception e) {
                throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
            }
            /*
            BlobClient blob = blobContainerClient.getBlobClient(photoId);
            if(!blob.exists()) { throw new WebApplicationException(Response.Status.NOT_FOUND); }
            BinaryData data = blob.downloadContent();
            return data.toBytes();
            */
        }

        public boolean existsBlob(String photoId) {
            return (new File(persistentVolume+photoId).isFile());
        }

        public List<String> list(){
            return Stream.of(Objects.requireNonNull(new File(persistentVolume).listFiles())).map(File::getName).collect(Collectors.toList());
            /*
            List<String> list = new ArrayList<>();
            for (BlobItem image : blobContainerClient.listBlobs()) {
                list.add(image.getName());
            }
            */
        }
    }