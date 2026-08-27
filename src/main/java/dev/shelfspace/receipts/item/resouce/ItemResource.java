package dev.shelfspace.receipts.item.resouce;

import dev.shelfspace.receipts.item.model.ItemPurchase;
import dev.shelfspace.receipts.item.service.ItemService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

/**
 * Exposes item-oriented receipt searches over HTTP.
 */
@Path("/api/items")
@Produces(MediaType.APPLICATION_JSON)
public class ItemResource {
    private final ItemService itemService;

    @Inject
    public ItemResource(ItemService itemService) {
        this.itemService = itemService;
    }

    @GET
    public List<ItemPurchase> search(@QueryParam("query") String query){
        /**
         * An empty substring matches every item. Rejecting blank searches
         * prevents clients from accidentally requesting full history.
         */
        if (query == null || query.isBlank()){
            throw new BadRequestException(
                    "The query parameter is required"
            );
        }
        return itemService.findPurchaseHistory(query);
    }
}
