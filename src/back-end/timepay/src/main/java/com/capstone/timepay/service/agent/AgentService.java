package com.capstone.timepay.service.agent;

import com.capstone.timepay.controller.agent.response.AgentUserInfoResponse;
import com.capstone.timepay.controller.agent.response.AgentUserRegisterResponse;
import com.capstone.timepay.domain.agent.Agent;
import com.capstone.timepay.domain.agent.AgentRepository;
import com.capstone.timepay.domain.user.User;
import com.capstone.timepay.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AgentService {

    private final UserRepository userRepository;
    private final AgentRepository agentRepository;

    public AgentUserRegisterResponse agentRegister(Long userId, User agent){
        User user = userRepository.findById(userId).orElseThrow(()
        -> new IllegalArgumentException("존재하지 않는 신청인 유저입니다."));

        AgentUserRegisterResponse agentUserRegisterResponse = new AgentUserRegisterResponse(
                userId, false, "알 수 없는 이유로 agentRegister 함수 실행 도중 실패했습니다.");
        if (agentRepository.findByCreatedUserAndAssignedUser(agent, user) == null) {
            agentRepository.save(Agent.builder()
                    .createdUser(agent)
                    .assignedUser(user)
                    .build()
            );
            agentUserRegisterResponse.setStatus(true);
            agentUserRegisterResponse.setContent("등록에 성공했습니다.");

        }
        else{
            agentUserRegisterResponse.setStatus(false);
            agentUserRegisterResponse.setContent("이미 등록된 대리인과 신청인입니다.");
        }
        return agentUserRegisterResponse;
    }

    public AgentUserInfoResponse agentInfo(User user){
        Agent agentInfo = agentRepository.findByAssignedUser(user);
        AgentUserInfoResponse agentUserInfoResponse = new AgentUserInfoResponse(false, null,
                null,null);

        if(agentInfo == null || agentInfo.getCreatedUser() == null){
            agentUserInfoResponse = new AgentUserInfoResponse(false, null,
                    user.getUserId(),user.getName());
        }
        else{
            agentUserInfoResponse = new AgentUserInfoResponse(true, agentInfo.getCreatedUser().getName(),
                    user.getUserId(),user.getName());
        }

        return agentUserInfoResponse;
    }

    public Boolean agentDelete(User user){
        agentRepository.delete(agentRepository.findByAssignedUser(user));
        return true;
    }
}