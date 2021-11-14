package io.agrest.provider;

import io.agrest.AgResponse;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * Ensures correct default response status for Agrest responses.
 * 
 * @since 1.1
 */
@Provider
public class ResponseStatusFilter implements ContainerResponseFilter {

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {

		Object entity = responseContext.getEntity();
		if (entity instanceof AgResponse) {

			AgResponse response = (AgResponse) entity;
			if (response.getStatus() > 0) {
				responseContext.setStatus(response.getStatus());
			}
		}
	}
}
