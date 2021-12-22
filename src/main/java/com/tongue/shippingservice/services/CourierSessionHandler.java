package com.tongue.shippingservice.services;

import com.tongue.shippingservice.domain.Artifact;
import com.tongue.shippingservice.domain.Courier;
import com.tongue.shippingservice.domain.Position;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class CourierSessionHandler {

    private FindByIndexNameSessionRepository sessionRepository;
    private CourierWsSessionHandler wsSessionHandler;

    public CourierSessionHandler(@Autowired FindByIndexNameSessionRepository sessionRepository,
                                 @Autowired CourierWsSessionHandler wsSessionHandler){
        this.sessionRepository=sessionRepository;
        this.wsSessionHandler=wsSessionHandler;
    }

    public Boolean updateCourierStatus(Courier.status status, Courier courier){
        log.info("Setting {Status:"+status.name()+",CourierUserName:"+courier.getUsername()+"}");
        try {
            Map<String, ? extends Session> userSessions =
                    sessionRepository.findByPrincipalName(courier.getUsername());
            for (Map.Entry<String, ? extends Session> entry:
                    userSessions.entrySet()) {
                Session session = entry.getValue();
                session.setAttribute("STATUS",status);
                sessionRepository.save(session);
            }
            return Boolean.TRUE;
        }catch (Exception e){
            return Boolean.FALSE;
        }
    }

    public Boolean removeArtifact(Courier courier){
        log.info("Removing Artifact from session");
        try {
            Map<String, ? extends Session> userSessions =
                    sessionRepository.findByPrincipalName(courier.getUsername());
            for (Map.Entry<String, ? extends Session> entry:
                    userSessions.entrySet()) {
                Session session = entry.getValue();
                session.removeAttribute("ARTIFACT");
                sessionRepository.save(session);
            }
            return Boolean.TRUE;
        }catch (Exception e){
            return Boolean.FALSE;
        }
    }

    public Boolean attachArtifact(Artifact artifact, Courier courier){
        log.info("Setting Artifact: "+artifact);
        try {
            Map<String, ? extends Session> userSessions =
                    sessionRepository.findByPrincipalName(courier.getUsername());
            for (Map.Entry<String, ? extends Session> entry:
                    userSessions.entrySet()) {
                Session session = entry.getValue();
                session.setAttribute("ARTIFACT",artifact);
                sessionRepository.save(session);
            }
            return Boolean.TRUE;
        }catch (Exception e){
            return Boolean.FALSE;
        }
    }

    public Artifact getArtifact(Courier courier){
        log.info("Getting Artifact from: "+courier.getUsername());
        Artifact artifact;
        Map<String, ? extends Session> userSessions =
                sessionRepository.findByPrincipalName(courier.getUsername());
        for (Map.Entry<String, ? extends Session> entry:
                    userSessions.entrySet()) {
            Session session = entry.getValue();
            artifact = session.getAttribute("ARTIFACT");
            return artifact;
        }
        return null;
    }

    public Boolean savePosition(Position position, Courier courier){
        try {
            Map<String,Session> sessionsMap =
                    sessionRepository.findByPrincipalName(courier.getUsername());
            log.info("Persisting position {latitude:"
                    +position.getLatitude()+",longitude:"+position.getLongitude()+"}");
            for (Map.Entry<String,Session> sessionMap:
                    sessionsMap.entrySet()) {
                Session session = sessionMap.getValue();
                session.setAttribute("POSITION",position);
                sessionRepository.save(session);
            }
            return Boolean.TRUE;
        }catch (Exception e){
            return Boolean.FALSE;
        }
    }

    public List<Courier> getAllCouriersWithStatus(Courier.status status){
        log.warn("Iterating through the whole repository might cause performance issues");
        List<Courier> couriers = new ArrayList<>();
        for (SimpUser simpUser:
                wsSessionHandler.getAll()) {
            Boolean alreadyAccepted = Boolean.FALSE;
            String username = simpUser.getName();
            log.info("Candidate: "+username);
            Map<String, ? extends Session> sessions = sessionRepository.findByPrincipalName(username);
            for (Map.Entry<String, ? extends Session> entry:
                    sessions.entrySet()) {

                if (alreadyAccepted)
                    continue;
                Session session = entry.getValue();
                Courier.status courierStatus = session.getAttribute("STATUS");
                Position courierPosition = session.getAttribute("POSITION");
                if (courierStatus==null || courierPosition==null){
                    log.warn("Status or Position attributes shouldn't be null");
                    continue;
                }
                log.info("Status: "+courierStatus);
                if (courierStatus!= status){
                    log.info("Candidate rejected");
                    continue;
                }
                log.info("Candidate accepted");
                Courier courier = Courier.builder().position(courierPosition).username(username).build();
                couriers.add(courier);
                alreadyAccepted=Boolean.TRUE;
            }
        }
        return couriers;
    }
}
