-- Copyright 2019 Oath Inc.
-- Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.

--- a module provides default strategies, including constants and variables, in a centralized way.
-- @module Utils

local misc = require 'utils/misc'

local M = {}

--- Constants in all lua files:
-- Dimensions
M.DEFAULT_DIMENSION_TYPE = "KeyValueStoreDimension"
M.DEFAULT_DIMENSION_CATEGORY = "UNKNOWN_CATEGORY"
M.DEFAULT_DIMENSION_IS_AGGREGATABLE = true
M.DEFAULT_DIMENSION_FIELDS = {}
M.DEFAULT_DIMENSION_KEY_VALUE_STORE = "memory"
-- Physical Tables
M.DEFAULT_PHYSICAL_TYPE = "strict"
M.DEFAULT_PHYSICAL_DEPENDENT_TABLE = {}
M.DEFAULT_PHYSICAL_DATE_TIME_ZONE = "UTC"
M.DEFAULT_PHYSICAL_GRANULARITY = "day"
M.DEFAULT_PHYSICAL_COLUMN_NAME_MAP = {}
-- Logical Tables
M.DEFAULT_LOGICAL_TYPE = "default"
M.DEFAULT_LOGICAL_DEPENDENT_TABLE = {}
M.DEFAULT_LOGICAL_DATE_TIME_ZONE = "UTC"
--- Defaulting methods:
-- Dimensions
function M.dimension_defaulting(dimension_name, dimension)
    local dim_config = misc.shallow_copy(dimension)
    dim_config.longName = dim_config.longName or dimension_name
    dim_config.description = dim_config.description or dimension_name
    dim_config.domain = dim_config.domain or dimension_name
    dim_config.type = dim_config.type or M.DEFAULT_DIMENSION_TYPE
    dim_config.category = dim_config.category or M.DEFAULT_DIMENSION_CATEGORY
    dim_config.defaultFields = dim_config.defaultFields or M.DEFAULT_DIMENSION_FIELDS
    dim_config.keyValueStore = dim_config.keyValueStore or M.DEFAULT_DIMENSION_KEY_VALUE_STORE
    if dim_config.isAggregatable == nil then
        dim_config.isAggregatable = M.DEFAULT_DIMENSION_IS_AGGREGATABLE
    end
    return dim_config
end

function M.physical_table_defaulting(physical_table_name, physical_table)
    local phys_config = misc.shallow_copy(physical_table)
    phys_config.description = phys_config.description or physical_table_name
    phys_config.type = phys_config.type or M.DEFAULT_PHYSICAL_TYPE
    phys_config.physicalTables = phys_config.physicalTables or M.DEFAULT_PHYSICAL_DEPENDENT_TABLE
    phys_config.dateTimeZone = phys_config.dateTimeZone or M.DEFAULT_PHYSICAL_DATE_TIME_ZONE
    phys_config.granularity = phys_config.granularity or M.DEFAULT_PHYSICAL_GRANULARITY
    phys_config.logicalToPhysicalColumnNames = phys_config.logicalToPhysicalColumnNames
            or M.DEFAULT_PHYSICAL_COLUMN_NAME_MAP
    return phys_config
end

function M.logical_table_defaulting(logical_table_name, logical_table)
    local logi_config = misc.shallow_copy(logical_table)
    logi_config.description = logi_config.description or logical_table_name
    logi_config.type = logi_config.type or M.DEFAULT_LOGICAL_TYPE
    logi_config.physicalTables = logi_config.physicalTables or M.DEFAULT_LOGICAL_DEPENDENT_TABLE
    logi_config.dateTimeZone = logi_config.dateTimeZone or M.DEFAULT_LOGICAL_DATE_TIME_ZONE
    return logi_config
end
return M