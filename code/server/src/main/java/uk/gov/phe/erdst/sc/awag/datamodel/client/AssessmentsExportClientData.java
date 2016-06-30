package uk.gov.phe.erdst.sc.awag.datamodel.client;

import javax.validation.constraints.NotNull;

import uk.gov.phe.erdst.sc.awag.service.validation.annotations.ValidIdsArray;

public class AssessmentsExportClientData
{
    @NotNull
    @ValidIdsArray
    public Long[] ids;
}
