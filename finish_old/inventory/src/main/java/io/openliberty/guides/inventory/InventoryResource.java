// tag::copyright[]
/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial implementation
 *******************************************************************************/
// end::copyright[]
package io.openliberty.guides.inventory;

import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import io.openliberty.guides.models.CpuUsage;
import io.openliberty.guides.models.MemoryStatus;


@ApplicationScoped
@Path("/inventory")
public class InventoryResource {

    private static Logger logger = Logger.getLogger(InventoryResource.class.getName());

    @Inject
    private InventoryManager manager;

    @GET
    @Path("/systems")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSystems() {
        List<Properties> systems = manager.getSystems()
                .values()
                .stream()
                .collect(Collectors.toList());
        return Response
                .status(Response.Status.OK)
                .entity(systems)
                .build();
    }

    @GET
    @Path("/system/{hostId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSystem(@PathParam("hostId") String hostId) {
        Optional<Properties> system = manager.getSystem(hostId);
        if (system.isPresent()) {
            return Response
                    .status(Response.Status.OK)
                    .entity(system)
                    .build();
        }
        return Response
                .status(Response.Status.NOT_FOUND)
                .entity("hostId does not exist.")
                .build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response resetSystems() {
        manager.resetSystems();
        return Response
                .status(Response.Status.OK)
                .build();
    }
    
    // tag::cpuStatus[]
    @Incoming("cpuStatus")
    // end::cpuStatus[]
    public void updateStatus(CpuUsage c)  {
        String hostId = c.hostId;
        if (manager.getSystem(hostId).isPresent()) {
            manager.updateCpuStatus(hostId, c.cpuUsage);
            logger.info("Host " + hostId + " was updated: " + c);
        } else {
            manager.addSystem(hostId, c.cpuUsage);
            logger.info("Host " + hostId + " was added: " + c);
        }
    }
    
    // tag::memoryStatus[]
    @Incoming("memoryStatus")
    // end::memoryStatus[]
    public void updateStatus(MemoryStatus m)  {
        String hostId = m.hostId;
        if (manager.getSystem(hostId).isPresent()) {
            manager.updateMemoryStatus(hostId, m.memoryUsed, m.memoryMax);
            logger.info("Host " + hostId + " was updated: " + m);
        } else {
            manager.addSystem(hostId, m.memoryUsed, m.memoryMax);
            logger.info("Host " + hostId + " was added: " + m);
        }
    }
}