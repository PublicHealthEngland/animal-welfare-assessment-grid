package uk.gov.phe.erdst.sc.awag.servlets.user;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Validator;

import uk.gov.phe.erdst.sc.awag.businesslogic.UserController;
import uk.gov.phe.erdst.sc.awag.datamodel.client.UserClientData;
import uk.gov.phe.erdst.sc.awag.datamodel.response.ResponsePayload;
import uk.gov.phe.erdst.sc.awag.exceptions.AWInvalidParameterException;
import uk.gov.phe.erdst.sc.awag.service.validation.utils.ValidatorUtils;
import uk.gov.phe.erdst.sc.awag.servlets.utils.RequestConverter;
import uk.gov.phe.erdst.sc.awag.servlets.utils.ResponseFormatter;
import uk.gov.phe.erdst.sc.awag.servlets.utils.ServletConstants;
import uk.gov.phe.erdst.sc.awag.servlets.utils.ServletSecurityUtils;
import uk.gov.phe.erdst.sc.awag.servlets.utils.ServletUtils;

@SuppressWarnings("serial")
@WebServlet({"/user"})
@ServletSecurity(@HttpConstraint(rolesAllowed = {ServletSecurityUtils.RolesAllowed.AW_ADMIN}))
public class UserServlet extends HttpServlet
{
    @Inject
    private RequestConverter mRequestConverter;

    @Inject
    private UserController mUserController;

    @Inject
    private ResponseFormatter mResponseFormatter;

    @Inject
    private Validator mRequestValidator;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        String callback;
        ServletUtils.setJsonContentType(response);
        ResponsePayload responsePayload = new ResponsePayload();
        try
        {
            callback = ServletUtils.getCallbackParameter(request);
            try
            {
                String action = ServletUtils.getSelectActionParameter(request);
                executeAction(action, request, responsePayload);
            }
            catch (AWInvalidParameterException ex)
            {
                responsePayload.addError(ex.getMessage());
            }
            finally
            {
                ServletUtils.printResponse(response, callback, mResponseFormatter.toJson(responsePayload));
            }
        }
        catch (AWInvalidParameterException ex)
        {
            responsePayload.addError(ex.getMessage());
            ServletUtils.setResponseBody(response, mResponseFormatter.toJson(responsePayload));
            ServletUtils.setResponseClientError(response);
        }
    }

    private void executeAction(String action, HttpServletRequest request, ResponsePayload responsePayload)
        throws AWInvalidParameterException
    {
        Object payload = ServletUtils.getNoResponsePayload();

        boolean includeMetadata = ServletUtils.getIncludeMetadataParameter(request);
        Integer offset = ServletUtils.getOffsetParameter(request);
        Integer limit = ServletUtils.getLimitParameter(request);

        switch (action)
        {
            case ServletConstants.REQ_PARAM_SELECT_ACTION_ALL:
                // XXX: if enabled make sure to return a user DTO and not the actual entity
                // payload = mUserController.getUsers(offset, limit, responsePayload);
                payload = ServletUtils.getNoResponsePayload();
                break;
            case ServletConstants.REQ_PARAM_SELECT_ACTION_LIKE:
                String like = ServletUtils.getSelectLikeParameter(request);
                ValidatorUtils.validateRequest(ServletUtils.getPagingRequestParams(offset, limit), responsePayload,
                    mRequestValidator);

                if (!responsePayload.hasErrors())
                {
                    payload = mUserController.getUsersLike(like, offset, limit, responsePayload, includeMetadata);
                }
                break;
            default:
                payload = ServletUtils.getNoResponsePayload();
                break;
        }

        responsePayload.setData(payload);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
        IOException
    {
        String userJson = request.getParameter("user");

        UserClientData clientData = (UserClientData) mRequestConverter.convert(userJson, UserClientData.class);

        ResponsePayload responsePayload = new ResponsePayload();

        mUserController.storeUser(clientData, responsePayload);

        if (responsePayload.getErrors().size() > 0)
        {
            ServletUtils.setResponseClientError(response);
        }
        else
        {
            ServletUtils.setResponseCreated(response);
        }
        ServletUtils.setResponseBody(response, mResponseFormatter.toJson(responsePayload));
    }
}