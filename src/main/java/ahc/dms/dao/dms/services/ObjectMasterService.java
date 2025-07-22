package ahc.dms.dao.dms.services;

import ahc.dms.dao.dms.entities.ObjectMaster;
import ahc.dms.dao.dms.repositories.ObjectMasterRepository;
import ahc.dms.exceptions.ApiException;
import ahc.dms.exceptions.DuplicateResourceException;
import ahc.dms.exceptions.ResourceNotFoundException;
import ahc.dms.payload.dto.ObjectMasterDto;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ObjectMasterService {

    @Autowired
    private ObjectMasterRepository omRepository;
    @Autowired
    private ModelMapper modelMapper;
    private final Logger logger = LoggerFactory.getLogger(ObjectMasterService.class);


    @Transactional(transactionManager = "dmsTransactionManager")
    public ObjectMasterDto createObjectMaster(@Valid ObjectMasterDto omDto) {


        ObjectMaster om = omRepository
                .findByRequestUriAndRequestMethod(omDto.getRequestUri(), omDto.getRequestMethod())
                .map(objectMaster -> {
                    if (Boolean.FALSE.equals(objectMaster.getStatus())) {
                        throw new ApiException("Object (url) is disabled");
                    } else if (Boolean.TRUE.equals(objectMaster.getStatus())) {
                        throw new DuplicateResourceException(objectMaster.getRequestUri(), objectMaster.getRequestMethod());
                    }
                    return objectMaster;
                })
                .orElseGet(() -> {
                    ObjectMaster newOm = modelMapper.map(omDto, ObjectMaster.class);
                    newOm.setStatus(Boolean.TRUE);
                    return omRepository.save(newOm);
                });

        return modelMapper.map(om, ObjectMasterDto.class);

    }

    @Transactional(transactionManager = "dmsTransactionManager")
    public ObjectMasterDto enableObjectMaster(Long omId) {

        ObjectMaster enabledOm = omRepository.findById(omId)
                .map(om -> {
                    if (Boolean.TRUE.equals(om.getStatus())) {
                        throw new ApiException("Object (url) already enabled");
                    }
                    om.setStatus(Boolean.TRUE);
                    return omRepository.save(om);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Object (url)", "Object Id", omId));

        return modelMapper.map(enabledOm, ObjectMasterDto.class);
    }

    @Transactional(transactionManager = "dmsTransactionManager")
    public ObjectMasterDto disableObjectMaster(Long omId) {
        ObjectMaster disabledOm = omRepository.findById(omId)
                .map(om -> {
                    if (Boolean.FALSE.equals(om.getStatus())) {
                        throw new ApiException("Object (url) already disabled");
                    }
                    om.setStatus(Boolean.FALSE);
                    return omRepository.save(om);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Object (url)", "Object Id", omId));

        return modelMapper.map(disabledOm, ObjectMasterDto.class);
    }
}
