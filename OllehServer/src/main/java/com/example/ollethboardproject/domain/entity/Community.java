package com.example.ollethboardproject.domain.entity;

import com.example.ollethboardproject.controller.request.community.CommunityCreateRequest;
import com.example.ollethboardproject.controller.request.community.CommunityUpdateRequest;
import com.example.ollethboardproject.domain.entity.audit.AuditEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "community")
@Setter
@NoArgsConstructor
public class Community extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "region")
    private String region;
    @Column(name = "interest")
    private String interest;
    @Column(name = "community_name")
    private String communityName;
    @Column(name = "info", columnDefinition = "text")
    private String info;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private Image image;
    @OneToOne(mappedBy = "community", cascade = CascadeType.ALL)
    @JoinColumn(name = "chatroom_id")
    private ChatRoom chatRoom;
    @OneToMany(mappedBy = "community")
    private List<Olleh> ollehsList = new ArrayList<>();

    private Community(String region, String interest, String info, String communityName, Member member) {
        this.region = region;
        this.interest = interest;
        this.info = info;
        this.communityName = communityName;
        this.member = member;
    }

    public static Community of(CommunityCreateRequest communityCreateRequest, Member member) {
        return new Community(
                communityCreateRequest.getRegion(),
                communityCreateRequest.getInterest(),
                communityCreateRequest.getInfo(),
                communityCreateRequest.getCommunityName(),
                member);
    }

    public void update(CommunityUpdateRequest communityUpdateRequest, Member member) {
        this.region = communityUpdateRequest.getRegion();
        this.interest = communityUpdateRequest.getInterest();
        this.info = communityUpdateRequest.getInfo();
        this.communityName = communityUpdateRequest.getCommunityName();
        this.member = member;
    }

    public void updateImage(Image image) {
        this.image = image;
    }

    public void setChatRoom(ChatRoom chatRoom) {

        if (this.chatRoom != null) {
            this.chatRoom.setCommunity(null);
        }
        this.chatRoom = chatRoom;
        chatRoom.setCommunity(this);
    }
}