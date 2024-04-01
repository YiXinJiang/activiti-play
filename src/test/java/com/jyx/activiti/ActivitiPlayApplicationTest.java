package com.jyx.activiti;

import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: ActivitiPlayApplicationTest
 * @Description:
 * @Author: jyx
 * @Date: 2024-03-29 11:53
 **/
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ActivitiPlayApplicationTest {

    @Autowired
    private ProcessEngine processEngine;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private TaskService taskService;

    @Test
    public void test() throws FileNotFoundException {
        String processDefinitionFilePath = "D:\\project\\activiti-play\\src\\main\\resource\\leave.bpmn20.xml";
        Deployment deploy = this.repositoryService.createDeployment()
                .addInputStream(processDefinitionFilePath, new FileInputStream(processDefinitionFilePath))
                .deploy();
        log.info("部署流程定义成功：{}", deploy);
    }

    @Test
    public void startProcessInstance() {
        //启动流程时传递的参数列表 这里根据实际情况 也可以选择不传
        Map<String, Object> variables = new HashMap<>();
        variables.put("姓名", "张三");
        variables.put("请假天数", 4);
        variables.put("请假原因", "我很累！");

        // 根据流程定义ID查询流程定义  leave:1:10004是我们刚才部署的流程定义的id
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId("leave:1:5004")
                .singleResult();

        // 获取流程定义的Key
        String processDefinitionKey = processDefinition.getKey();

        // 定义businessKey  businessKey一般为流程实例key与实际业务数据的结合
        // 假设一个请假的业务 在数据库中的id是1001
        String businessKey = processDefinitionKey + ":" + "1001";
        // 设置启动流程的人
        Authentication.setAuthenticatedUserId("xxyin");
        ProcessInstance processInstance =
                runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, variables);

        log.info("流程启动成功：{}", processInstance);
    }

    @Test
    public void findTodoTask() {
        TaskQuery taskQuery = taskService.createTaskQuery().orderByTaskCreateTime().asc();
        // <userTask id="deptLeaderVerify" name="部门领导审批" activiti:assignee="zhangsan" ></userTask>
        //添加查询条件 查询指派给 zhangsan 的任务  假设这个任务指派给了zhangsan
        taskQuery.taskAssignee("zhangsan");
        //添加查询条件 查询流程定义key为 leave 的任务
        taskQuery.processDefinitionKey("leave");
        List<Task> tasks = taskQuery.list();
        // 处理查询结果
        for (Task task : tasks) {
            log.info("Task ID: {}", task.getId());
            log.info("Task Name: {}", task.getName());
            // 其他任务属性的获取和处理
        }
    }

}
