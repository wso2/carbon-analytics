package org.wso2.carbon.stream.processor.auth.rest.api.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.stream.processor.auth.rest.api.NotFoundException;
import org.wso2.carbon.stream.processor.auth.rest.api.UserstoreApiService;
import org.wso2.carbon.stream.processor.auth.rest.api.dto.ErrorDTO;
import org.wso2.carbon.stream.processor.auth.rest.api.dto.GroupDTO;
import org.wso2.carbon.stream.processor.auth.rest.api.internal.DataHolder;
import org.wso2.carbon.stream.processor.idp.client.core.exception.IdPClientException;
import org.wso2.msf4j.Request;

import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response;

public class UserstoreApiServiceImpl extends UserstoreApiService {

    private static final Logger LOG = LoggerFactory.getLogger(UserstoreApiServiceImpl.class);

    @Override
    public Response userstoreGroupsGet(Request request) throws NotFoundException {
        try {
            List<GroupDTO> collect = DataHolder.getInstance().getIdPClient().getAllGroups().stream().map((group) -> {
                GroupDTO groupDTO = new GroupDTO();
                groupDTO.setDisplay(group.getDisplayName());
                groupDTO.setId(group.getId());
                return groupDTO;
            }).collect(Collectors.toList());
            return Response.ok().entity(collect).build();
        } catch (IdPClientException e) {
            ErrorDTO errorDTO = new ErrorDTO();
            errorDTO.setMessage(e.getMessage());
            LOG.error(e.getMessage(), e);
            return Response.serverError().entity(errorDTO).build();
        }
    }

    @Override
    public Response userstoreUsersNameGroupsGet(String name, Request request) throws NotFoundException {
        try {
            List<GroupDTO> usersGroups = DataHolder.getInstance().getIdPClient().getUsersGroups(name).stream().map((group) -> {
                GroupDTO groupDTO = new GroupDTO();
                groupDTO.setDisplay(group.getDisplayName());
                groupDTO.setId(group.getId());
                return groupDTO;
            }).collect(Collectors.toList());
            return Response.ok().entity(usersGroups).build();
        } catch (IdPClientException e) {
            ErrorDTO errorDTO = new ErrorDTO();
            errorDTO.setMessage(e.getMessage());
            LOG.error(e.getMessage(), e);
            return Response.serverError().entity(errorDTO).build();
        }
    }
}
