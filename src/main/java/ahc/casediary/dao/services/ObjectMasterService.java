package ahc.casediary.dao.services;

import ahc.casediary.dao.entities.ObjectMaster;
import ahc.casediary.dao.repositories.ObjectMasterRepository;
import ahc.casediary.exceptions.ApiException;
import ahc.casediary.exceptions.DuplicateResourceException;
import ahc.casediary.exceptions.ResourceNotFoundException;
import ahc.casediary.payload.dto.ObjectMasterDto;
import ahc.casediary.payload.response.PageResponse;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ObjectMasterService {

    @Autowired
    private ObjectMasterRepository omRepository;
    @Autowired
    private ModelMapper modelMapper;
    private final Logger logger = LoggerFactory.getLogger(ObjectMasterService.class);


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

    @Transactional
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

    @Transactional
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

    // Find all url objects
    @Transactional
    public PageResponse<ObjectMasterDto> getAllUriObjects(int pageNumber, int pageSize, String sortBy, String sortDir){

        Sort sort = sortDir.equalsIgnoreCase(sortDir)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize,sort);
        Page<ObjectMaster> userPage = omRepository.findAll(pageable);
        List<ObjectMasterDto> objectMasterDto = userPage.getContent().stream()
                .map(ObjectMaster -> modelMapper.map(ObjectMaster, ObjectMasterDto.class))
                .collect(Collectors.toList());

        PageResponse<ObjectMasterDto> response = new PageResponse<>();
        response.setContent(objectMasterDto);
        response.setPageNumber(userPage.getNumber());
        response.setPageSize(userPage.getSize());
        response.setTotalElements(userPage.getTotalElements());
        response.setTotalPages(userPage.getTotalPages());
        response.setLastPage(userPage.isLast());

        return response;
    }
}
