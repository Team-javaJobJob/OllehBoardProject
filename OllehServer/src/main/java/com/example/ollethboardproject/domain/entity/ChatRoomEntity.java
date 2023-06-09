package com.example.ollethboardproject.domain.entity;


import com.example.ollethboardproject.controller.Status;
import com.example.ollethboardproject.domain.entity.audit.AuditEntity;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;


// 채팅룸을 위한 Entity
@Entity
@Getter
@NoArgsConstructor
@Data
@Table(name = "chatRoom_table")
public class ChatRoomEntity extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;
    @Column
    private String senderName;
    @Column
    private String receiverName;
    @Column
    private String message;
    private Date date;
    @Enumerated(EnumType.STRING)
    private Status status;

}
