/*
 * Copyright 2017 Danish Maritime Authority.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package net.maritimecloud.endorsement.controllers;

import net.maritimecloud.endorsement.model.data.EndorsementList;
import net.maritimecloud.endorsement.model.db.Endorsement;
import net.maritimecloud.endorsement.services.EndorsementService;
import net.maritimecloud.endorsement.validators.EndorsementValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
//@RequestMapping(value={"oidc", "x509"})
@RequestMapping(value="oidc")
public class EndorseController {

    @Autowired
    private EndorsementValidator endorsementValidator;

    @Autowired
    private EndorsementService endorsementService;

    @InitBinder("endorsement")
    protected void initBinder(final ServletRequestDataBinder binder) {
        binder.addValidators(endorsementValidator);
    }


    @RequestMapping(
            value = "/endorsements",
            method = RequestMethod.POST,
            consumes = "application/json;charset=UTF-8",
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    @PreAuthorize("@accessControlUtil.hasAccessToOrg(#input.getOrgMrn())")
    public ResponseEntity<Endorsement> createEndorment(HttpServletRequest request, @Validated @RequestBody Endorsement input) {
        Endorsement endorsement = this.endorsementService.getByOrgMrnAndServiceMrnAndServiceVersion(input.getOrgMrn(), input.getServiceMrn(), input.getServiceVersion());
        if (endorsement != null) {
            endorsement.setUserMrn(input.getUserMrn());
            endorsementService.saveEndorsement(endorsement);
        } else {
            endorsement = endorsementService.saveEndorsement(input);
        }
        return new ResponseEntity<>(endorsement, HttpStatus.OK);
    }

    @RequestMapping(
            value = "/endorsements/{serviceMrn}/{serviceVersion}",
            method = RequestMethod.GET,
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    public Page<Endorsement> getEndormentsByServiceMrn(HttpServletRequest request, @PathVariable String serviceMrn, @PathVariable String serviceVersion, Pageable pageable) {
        return endorsementService.listByServiceMrnAndServiceVersion(serviceMrn, serviceVersion, pageable);
    }

    /*@RequestMapping(
            value = "/endorsement-list",
            method = RequestMethod.POST,
            consumes = "application/json;charset=UTF-8",
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    public List<EndorsementList> getEndormentsByServiceMrns(HttpServletRequest request, @RequestBody List<String> serviceMrns) {
        List<EndorsementList> endorsementLists = new ArrayList<>();
        if (serviceMrns == null || serviceMrns.isEmpty()) {
            return endorsementLists;
        }
        for(String serviceMrn : serviceMrns) {
            EndorsementList endorsementList = new EndorsementList();
            endorsementList.setServiceMrn(serviceMrn);
            endorsementList.setEndorsements(endorsementService.listByServiceMrn(serviceMrn));
            endorsementLists.add(endorsementList);
        }
        return endorsementLists;
    }*/

    @RequestMapping(
            value = "/endorsements-by/{serviceLevel}/{orgMrn}",
            method = RequestMethod.GET,
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    public Page<Endorsement> getEndormentsByOrgMrn(HttpServletRequest request, @PathVariable String serviceLevel, @PathVariable String orgMrn, Pageable pageable) {
        return endorsementService.listByOrgMrnAndServiceLevel(orgMrn, serviceLevel, pageable);
    }

    @RequestMapping(
            value = "/endorsements/{serviceMrn}/{serviceVersion}/{orgMrn}",
            method = RequestMethod.DELETE)
    @ResponseBody
    @PreAuthorize("@accessControlUtil.hasAccessToOrg(#orgMrn)")
    public ResponseEntity<?> deleteEndorment(HttpServletRequest request, @PathVariable String serviceMrn, @PathVariable String serviceVersion, @PathVariable String orgMrn) {
        Endorsement endorsement = this.endorsementService.getByOrgMrnAndServiceMrnAndServiceVersion(orgMrn, serviceMrn, serviceVersion);
        if (endorsement == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        endorsementService.deleteEndorsement(endorsement);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(
            value = "/endorsement-by/{serviceMrn}/{serviceVersion}/{orgMrn}",
            method = RequestMethod.GET,
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    public ResponseEntity<?> getEndorsment(HttpServletRequest request, @PathVariable String serviceMrn, @PathVariable String serviceVersion, @PathVariable String orgMrn) {
        Endorsement endorsement = this.endorsementService.getByOrgMrnAndServiceMrnAndServiceVersion(orgMrn, serviceMrn, serviceVersion);
        if (endorsement == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(endorsement, HttpStatus.OK);
    }

    @RequestMapping(
            value = "/endorsed-children/{parentMrn}/{parentVersion}",
            method = RequestMethod.GET,
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    public Page<Endorsement> getEndorsedByParentMrn(HttpServletRequest request, @PathVariable String parentMrn, @PathVariable String parentVersion, Pageable pageable) {
        return endorsementService.listByParentMrnAndParentVersion(parentMrn, parentVersion, pageable);
    }

    @RequestMapping(
            value = "/endorsed-children/{parentMrn}/{parentVersion}/{orgMrn}",
            method = RequestMethod.GET,
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    public Page<Endorsement> getEndorsedByParentMrnAndOrgMrn(HttpServletRequest request, @PathVariable String parentMrn, @PathVariable String parentVersion, @PathVariable String orgMrn, Pageable pageable) {
        return endorsementService.listByParentMrnAndOrgMrn(parentMrn, parentVersion, orgMrn, pageable);
    }

}
