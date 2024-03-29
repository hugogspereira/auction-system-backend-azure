package scc.layers;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.SearchClientBuilder;
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.util.SearchPagedIterable;
import com.azure.search.documents.util.SearchPagedResponse;

import java.util.ArrayList;
import java.util.List;

public class CognitiveSearchLayer {

    private static CognitiveSearchLayer instance;

    private SearchClient sc;

    public static synchronized CognitiveSearchLayer getInstance() {
        if( instance != null)
            return instance;

        String searchServiceQueryKey = System.getenv("COGNITIVESEARCH_QUERYKEY");
        String searchServiceUrl = System.getenv("COGNITIVESEARCH_URL");
        String indexName = System.getenv("COGNITIVESEARCH_INDEX");

        SearchClient sc = new SearchClientBuilder()
                .credential(new AzureKeyCredential(searchServiceQueryKey))
                .endpoint(searchServiceUrl)
                .indexName(indexName)
                .buildClient();

        return instance = new CognitiveSearchLayer(sc);
    }

    public CognitiveSearchLayer(SearchClient sc) {
        this.sc = sc;
    }


    public List<Object> searchAuctions(String query) {

        SearchOptions options = new SearchOptions()
                .setIncludeTotalCount(true)
                .setSelect("id", "title", "description", "ownerNickname", "endTime")
                .setSearchFields("description")
                .setTop(10);

        SearchPagedIterable spi = sc.search(query, options, null);

        List<Object> results = new ArrayList<>();

        for(SearchPagedResponse resultResponse : spi.iterableByPage()) {
            resultResponse.getValue().forEach(searchResult -> {
                results.add(searchResult.getDocument(SearchDocument.class));
            });
        }

        return results;
    }

}
