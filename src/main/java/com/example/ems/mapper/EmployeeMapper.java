package com.example.ems.mapper;

import com.example.ems.entity.Employee;
import com.example.ems.entity.models.EmployeeModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    /* ===================== DTO → ENTITY ===================== */
    @Mappings({
            @Mapping(target = "id", source = "id"),
            @Mapping(target = "name", source = "name"),
            @Mapping(target = "email", source = "email"),
            @Mapping(target = "department", source = "department"),
            @Mapping(target = "salary", source = "salary"),
            @Mapping(target = "joiningDate", source = "joiningDate")
    })
    Employee toEmployeeEntity(EmployeeModel model);

    /* ===================== ENTITY → DTO ===================== */
    @Mappings({
            @Mapping(target = "id", source = "id"),
            @Mapping(target = "name", source = "name"),
            @Mapping(target = "email", source = "email"),
            @Mapping(target = "department", source = "department"),
            @Mapping(target = "salary", source = "salary"),
            @Mapping(target = "joiningDate", source = "joiningDate")
    })
    EmployeeModel toEmployeeModel(Employee employee);

    /* ===================== UPDATE EXISTING ENTITY ===================== */
    @Mappings({
            @Mapping(target = "id", ignore = true) // 🔥 NEVER update ID
    })
    void updateEntityFromModel(EmployeeModel model,
                               @MappingTarget Employee employee);
}
