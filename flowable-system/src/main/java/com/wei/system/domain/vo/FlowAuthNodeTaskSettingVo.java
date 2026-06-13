package com.wei.system.domain.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


/**
 * @author yuwei
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class FlowAuthNodeTaskSettingVo implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private String widgetCode;
	private String widgetName;
	@Builder.Default
	private Boolean viewable = false;
	@Builder.Default
	private Boolean editable = false;
}
