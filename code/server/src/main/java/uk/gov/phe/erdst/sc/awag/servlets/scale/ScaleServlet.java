package uk.gov.phe.erdst.sc.awag.servlets.scale;

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
import javax.ws.rs.HttpMethod;

import uk.gov.phe.erdst.sc.awag.businesslogic.ScaleController;
import uk.gov.phe.erdst.sc.awag.datamodel.client.ScaleClientData;
import uk.gov.phe.erdst.sc.awag.datamodel.response.ResponsePayload;
import uk.gov.phe.erdst.sc.awag.exceptions.AWInvalidParameterException;
import uk.gov.phe.erdst.sc.awag.exceptions.AWInvalidResourceIdException;
import uk.gov.phe.erdst.sc.awag.exceptions.AWNoSuchEntityException;
import uk.gov.phe.erdst.sc.awag.service.validation.utils.ValidatorUtils;
import uk.gov.phe.erdst.sc.awag.servlets.utils.RequestConverter;
import uk.gov.phe.erdst.sc.awag.servlets.utils.ResponseFormatter;
import uk.gov.phe.erdst.sc.awag.servlets.utils.ServletConstants;
import uk.gov.phe.erdst.sc.awag.servlets.utils.ServletSecurityUtils;
import uk.gov.phe.erdst.sc.awag.servlets.utils.ServletUtils;

@WebServlet(name = "scale", urlPatterns = {"/scale/*"})
@ServletSecurity(@HttpConstraint(rolesAllowed = {ServletSecurityUtils.RolesAllowed.AW_ASSESSMENT_USER}))
public class ScaleServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    @Inject
    private ScaleController mScalesController;

    @Inject
    private ResponseFormatter mResponseFormatter;

    @Inject
    private RequestConverter mRequestConverter;

    @Inject
    private Validator mRequestValidator;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        String callback;
        ResponsePayload responsePayload = new ResponsePayload();
        ServletUtils.setJsonContentType(response);
        try
        {
            callback = ServletUtils.getCallbackParameter(request);
            try
            {
                String action = ServletUtils.getSelectActionParameter(request);
                executeAction(action, request, responsePayload);
            }
            catch (AWInvalidParameterException | AWInvalidResourceIdException | AWNoSuchEntityException ex)
            {
                responsePayload.addError(ex.getMessage());
                ServletUtils.setResponseClientError(response);
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

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        String scalesJson = ServletUtils.getRequestBody(request);
        ScaleClientData clientData = (ScaleClientData) mRequestConverter.convert(scalesJson, ScaleClientData.class);
        ResponsePayload responsePayload = new ResponsePayload();
        Long scaleId = ServletUtils.getNumberResourceId(request);

        if (!ValidatorUtils.isResourceValid(scaleId, HttpMethod.PUT))
        {
            ServletUtils.setResponseResourceNotFound(response);
            return;
        }

        mScalesController
            .updateScale(scaleId, clientData, responsePayload, ServletSecurityUtils.getLoggedUser(request));

        if (responsePayload.getErrors().size() > 0)
        {
            ServletUtils.setResponseClientError(response);
        }
        else
        {
            ServletUtils.setResponseOk(response);
        }

        ServletUtils.setResponseBody(response, mResponseFormatter.toJson(responsePayload));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        String requestScalesJson = request.getParameter("scale");
        ScaleClientData clientData = (ScaleClientData) mRequestConverter.convert(requestScalesJson,
            ScaleClientData.class);
        ResponsePayload responsePayload = new ResponsePayload();

        mScalesController.storeScale(clientData, responsePayload, ServletSecurityUtils.getLoggedUser(request));

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

    private void executeAction(String action, HttpServletRequest request, ResponsePayload responsePayload)
        throws AWInvalidParameterException, AWInvalidResourceIdException, AWNoSuchEntityException
    {
        Object payload = ServletUtils.getNoResponsePayload();

        boolean includeMetadata = ServletUtils.getIncludeMetadataParameter(request);
        Integer offset = ServletUtils.getOffsetParameter(request);
        Integer limit = ServletUtils.getLimitParameter(request);

        switch (action)
        {
            case ServletConstants.REQ_PARAM_SELECT_ACTION_ALL:
                ValidatorUtils.validateRequest(ServletUtils.getPagingRequestParams(offset, limit), responsePayload,
                    mRequestValidator);

                if (!responsePayload.hasErrors())
                {
                    payload = mScalesController.getScales(offset, limit, responsePayload, includeMetadata);
                }
                break;
            case ServletConstants.REQ_PARAM_SELECT_ACTION_SEL_ID:
                Long scaleId = ServletUtils.getParseResourceId(ServletUtils.getSelectEntityIdParameter(request));
                payload = mScalesController.getScale(scaleId);
                break;
            case ServletConstants.REQ_PARAM_SELECT_ACTION_LIKE:
                ValidatorUtils.validateRequest(ServletUtils.getPagingRequestParams(offset, limit), responsePayload,
                    mRequestValidator);

                if (!responsePayload.hasErrors())
                {
                    payload = mScalesController.getScalesLike(ServletUtils.getSelectLikeParameter(request), offset,
                        limit, responsePayload, includeMetadata);
                }
                break;
            default:
                payload = ServletUtils.getNoResponsePayload();
                break;
        }

        responsePayload.setData(payload);
    }
}
