package delivery.service;

import delivery.entities.DeliveryAgent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import delivery.repository.DeliveryAgentRepository;
import delivery.repository.DeliveryRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class DeliveryService {
    @Autowired
    private DeliveryAgentRepository deliveryAgentRepository;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Transactional
    public synchronized long getAndAssignAgent(){
        List<DeliveryAgent> agents = deliveryAgentRepository.findByAvailableTrue(PageRequest.of(0, 1));
        if(!agents.isEmpty()){
            DeliveryAgent agent = agents.get(0);
            long agentId = agent.getId();
            agent.setAvailable(false);
            deliveryAgentRepository.saveAndFlush(agent);
            return agentId;
        }
        return -1;
    }

    @Transactional
    public void updateAgentAsAvailable(long agentId){
        Optional<DeliveryAgent> agentOptional = deliveryAgentRepository.findById(agentId);
        if(agentOptional.isPresent()){
            DeliveryAgent agent = agentOptional.get();
            agent.setAvailable(true);
            deliveryAgentRepository.saveAndFlush(agent);
        }
    }
}
