package com.bytechainx.psi.common.model;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.bytechainx.psi.common.EnumConstant.DataStatusEnum;
import com.bytechainx.psi.common.model.base.BaseGoodsSpec;

/**
 * 商品规格基础数据
 */
@SuppressWarnings("serial")
public class GoodsSpec extends BaseGoodsSpec<GoodsSpec> {
	
	public static final GoodsSpec dao = new GoodsSpec().dao();
	
	public GoodsSpec findById(Integer id) {
		return GoodsSpec.dao.findFirst("select * from goods_spec where id = ? and data_status != ? limit 1", id, DataStatusEnum.delete.getValue());
	}
	
	/**
	 * 规格下面所有的选项值
	 * @return
	 */
	public List<GoodsSpecOptions> getOptions() {
		return GoodsSpecOptions.dao.find("select * from goods_spec_options where goods_spec_id = ? and data_status = ?", getId(), DataStatusEnum.enable.getValue());
	}

	public List<GoodsSpec> findAll() {
		return GoodsSpec.dao.find("select * from goods_spec ");
	}

	public String getSperNameById(GoodsSpecDto dto) {
		if(dto.getSpecOption1Id() == null || dto.getSpecOption1Id() <= 0) {
			return "";
		}
		GoodsSpecOptions first = GoodsSpecOptions.dao.findById(dto.getSpecOption1Id());
		if(dto.getSpecOption2Id() == null || dto.getSpecOption2Id() <= 0) {
			return first.getOptionValue();
		}
		GoodsSpecOptions second = GoodsSpecOptions.dao.findById(dto.getSpecOption2Id());
		if(dto.getSpecOption3Id() == null || dto.getSpecOption3Id() <= 0) {
			return first.getOptionValue()+"/"+second.getOptionValue();
		}
		GoodsSpecOptions third = GoodsSpecOptions.dao.findById(dto.getSpecOption3Id());
		return first.getOptionValue()+"/"+second.getOptionValue()+"/"+third.getOptionValue();
	}
	
	public GoodsSpecDto getGoodsSpecDto(String goodsSpecIds) {
		GoodsSpecDto dto = new GoodsSpecDto();
		String[] specValues = StringUtils.split(goodsSpecIds, "|");
		if(specValues != null && specValues.length > 0) {
			String[] specs = StringUtils.split(specValues[0], ":");
			dto.setSpec1Id(Integer.parseInt(specs[0]));
			dto.setSpecOption1Id(Integer.parseInt(specs[1]));
		}
		if(specValues != null && specValues.length > 1) {
			String[] specs = StringUtils.split(specValues[1], ":");
			dto.setSpec2Id(Integer.parseInt(specs[0]));
			dto.setSpecOption2Id(Integer.parseInt(specs[1]));
		}
		if(specValues != null && specValues.length > 2) {
			String[] specs = StringUtils.split(specValues[2], ":");
			dto.setSpec3Id(Integer.parseInt(specs[0]));
			dto.setSpecOption3Id(Integer.parseInt(specs[1]));
		}
		return dto;
	}
	
	public class GoodsSpecDto {
		Integer spec1Id = 0;
		Integer specOption1Id = 0;
		Integer spec2Id = 0;
		Integer specOption2Id = 0;
		Integer spec3Id = 0;
		Integer specOption3Id = 0;
		public Integer getSpec1Id() {
			return spec1Id;
		}
		public void setSpec1Id(Integer spec1Id) {
			this.spec1Id = spec1Id;
		}
		public Integer getSpecOption1Id() {
			return specOption1Id;
		}
		public void setSpecOption1Id(Integer specOption1Id) {
			this.specOption1Id = specOption1Id;
		}
		public Integer getSpec2Id() {
			return spec2Id;
		}
		public void setSpec2Id(Integer spec2Id) {
			this.spec2Id = spec2Id;
		}
		public Integer getSpecOption2Id() {
			return specOption2Id;
		}
		public void setSpecOption2Id(Integer specOption2Id) {
			this.specOption2Id = specOption2Id;
		}
		public Integer getSpec3Id() {
			return spec3Id;
		}
		public void setSpec3Id(Integer spec3Id) {
			this.spec3Id = spec3Id;
		}
		public Integer getSpecOption3Id() {
			return specOption3Id;
		}
		public void setSpecOption3Id(Integer specOption3Id) {
			this.specOption3Id = specOption3Id;
		}
		
		
	}
}

