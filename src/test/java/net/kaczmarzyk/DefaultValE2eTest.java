/**
 * Copyright 2014-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.kaczmarzyk;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.kaczmarzyk.spring.data.jpa.Customer;
import net.kaczmarzyk.spring.data.jpa.CustomerRepository;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;


/**
 * @author Matt S.Y. Ho
 */
public class DefaultValE2eTest extends E2eTestBase {

  @Spec(path = "lastName", spec = Equal.class, defaultVal = "Simpson")
  public static interface DefaultSimpsonSpec extends Specification<Customer> {
  }

  @Controller
  public static class TestController {

    @Autowired
    CustomerRepository customerRepo;

    @RequestMapping("/defaultVal")
    @ResponseBody
    public Object listSimpsons(DefaultSimpsonSpec spec) {
      return customerRepo.findAll(spec);
    }

  }

  @Test
  public void filtersBySingleSpecWithoutParam() throws Exception {
      mockMvc.perform(get("/defaultVal")
              .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isArray())
          .andExpect(jsonPath("$[?(@.firstName=='Homer')]").exists())
          .andExpect(jsonPath("$[?(@.firstName=='Marge')]").exists())
          .andExpect(jsonPath("$[?(@.firstName=='Bart')]").exists())
          .andExpect(jsonPath("$[?(@.firstName=='Lisa')]").exists())
          .andExpect(jsonPath("$[?(@.firstName=='Maggie')]").exists())
          .andExpect(jsonPath("$[5]").doesNotExist());
  }

  @Test
  public void filtersBySingleSpecWithSingleDefaultVal() throws Exception {
      mockMvc.perform(get("/defaultVal")
              .param("lastName", "Szyslak")
              .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isArray())
          .andExpect(jsonPath("$[?(@.firstName=='Moe')]").exists())
          .andExpect(jsonPath("$[?(@.firstName=='Minnie')]").exists())
          .andExpect(jsonPath("$[2]").doesNotExist());
  }
}
