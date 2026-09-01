package dev.shelfspace.receipts.purchase.resource;

import dev.shelfspace.receipts.purchase.model.ItemPurchase;
import dev.shelfspace.receipts.purchase.service.ItemService;
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

    @GET
    @Path("/{sku}/purchases")
    public List<ItemPurchase> getPurchasesBySku(
            @PathParam("sku") String sku
    ) {
        List<ItemPurchase> purchases =
                itemService.findPurchasesBySku(sku);

        if (purchases.isEmpty()) {
            throw new NotFoundException(
                    "No purchase history was found for SKU '" + sku + "'."
            );
        }

        return purchases;
    }
}
