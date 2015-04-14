package com.example.valet.objectid;

import java.util.List;

import io.netty.handler.codec.http.HttpMethod;

import org.restexpress.Request;
import org.restexpress.Response;
import org.restexpress.common.query.QueryFilter;
import org.restexpress.common.query.QueryOrder;
import org.restexpress.common.query.QueryRange;
import org.restexpress.query.QueryFilters;
import org.restexpress.query.QueryOrders;
import org.restexpress.query.QueryRanges;
import com.example.valet.Constants;

import com.strategicgains.hyperexpress.HyperExpress;
import com.strategicgains.hyperexpress.builder.TokenBinder;
import com.strategicgains.hyperexpress.builder.TokenResolver;
import com.strategicgains.hyperexpress.builder.UrlBuilder;
import com.strategicgains.repoexpress.mongodb.Identifiers;

/**
 * This is the 'controller' layer, where HTTP details are converted to domain concepts and passed to the service layer.
 * Then service layer response information is enhanced with HTTP details, if applicable, for the response.
 * <p/>
 * This controller demonstrates how to process an entity that is identified by a MongoDB ObjectId.
 */
public class ValetController
{
	private static final UrlBuilder LOCATION_BUILDER = new UrlBuilder();
	private ValetService service;

	public ValetController(ValetService valetService)
	{
		super();
		this.service = valetService;
	}

	public Valet create(Request request, Response response)
	{
		Valet entity = request.getBodyAs(Valet.class, "Resource details not provided");
		Valet saved = service.create(entity);

		// Construct the response for create...
		response.setResponseCreated();

		// Bind the resource with link URL tokens, etc. here...
		TokenResolver resolver = HyperExpress.bind(Constants.Url.VALET_ID, Identifiers.MONGOID.format(saved.getId()));

		// Include the Location header...
		String locationPattern = request.getNamedUrl(HttpMethod.GET, Constants.Routes.SINGLE_VALET);
		response.addLocationHeader(LOCATION_BUILDER.build(locationPattern, resolver));

		// Return the newly-created resource...
		return saved;
	}

	public Valet read(Request request, Response response)
	{
		String id = request.getHeader(Constants.Url.VALET_ID, "No resource ID supplied");
		Valet entity = service.read(Identifiers.MONGOID.parse(id));

		// enrich the resource with links, etc. here...
		HyperExpress.bind(Constants.Url.VALET_ID, Identifiers.MONGOID.format(entity.getId()));

		return entity;
	}

	public List<Valet> readAll(Request request, Response response)
	{
		QueryFilter filter = QueryFilters.parseFrom(request);
		QueryOrder order = QueryOrders.parseFrom(request);
		QueryRange range = QueryRanges.parseFrom(request, 20);
		List<Valet> entities = service.readAll(filter, range, order);
		long count = service.count(filter);
		response.setCollectionResponse(range, entities.size(), count);

		// Bind the resources in the collection with link URL tokens, etc. here...
		HyperExpress.tokenBinder(new TokenBinder<Valet>()
		{
			@Override
			public void bind(Valet entity, TokenResolver resolver)
			{
				resolver.bind(Constants.Url.VALET_ID, Identifiers.MONGOID.format(entity.getId()));
			}
		});

		return entities;
	}

	public void update(Request request, Response response)
	{
		String id = request.getHeader(Constants.Url.VALET_ID, "No resource ID supplied");
		Valet entity = request.getBodyAs(Valet.class, "Resource details not provided");
		entity.setId(Identifiers.MONGOID.parse(id));
		service.update(entity);
		response.setResponseNoContent();
	}

	public void delete(Request request, Response response)
	{
		String id = request.getHeader(Constants.Url.VALET_ID, "No resource ID supplied");
		service.delete(Identifiers.MONGOID.parse(id));
		response.setResponseNoContent();
	}
}
