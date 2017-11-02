package org.wso2.carbon.status.dashboard.core.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * StatsEnable
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-11-02T13:49:11.445Z")
public class StatsEnable   {
  @JsonProperty("statsEnable")
  private Boolean statsEnable = null;

  public StatsEnable statsEnable(Boolean statsEnable) {
    this.statsEnable = statsEnable;
    return this;
  }

   /**
   * Get statsEnable
   * @return statsEnable
  **/
  @ApiModelProperty(required = true, value = "")
  public Boolean getStatsEnable() {
    return statsEnable;
  }

  public void setStatsEnable(Boolean statsEnable) {
    this.statsEnable = statsEnable;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    StatsEnable statsEnable = (StatsEnable) o;
    return Objects.equals(this.statsEnable, statsEnable.statsEnable);
  }

  @Override
  public int hashCode() {
    return Objects.hash(statsEnable);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class StatsEnable {\n");
    
    sb.append("    statsEnable: ").append(toIndentedString(statsEnable)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

