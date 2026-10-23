package com.taihoang.social_backend.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name= "messenger",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_messenger_conversation_user_client_message",
                        columnNames = {"conversation_id", "user_id", "client_message_id"}
                ),
                // Hai tin trong mot phong khong duoc trung so thu tu; index nay cung phuc vu phan trang theo sequence.
                @UniqueConstraint(
                        name = "uk_messenger_conversation_sequence",
                        columnNames = {"conversation_id", "sequence_number"}
                )
        })
@Setter
@Getter
public class Messenger {
    // Must match the 5000-char limit of MessageRequest/EditMessageRequest/StoryReplyRequest;
    // the old default VARCHAR(255) made MySQL reject longer messages.
    public static final int CONTENT_MAX_LENGTH = 5000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;
    @Column(name="content", length = CONTENT_MAX_LENGTH)
    private String content  ;
    @Column(name="client_message_id")
    private String clientMessageId ;
    @Column(name="sequence_number")
    private Long sequenceNumber ;
    @Column(name="sent_at")
    private LocalDateTime sentAt ;
    @Column(name="seen_at", columnDefinition = "datetime(6)")
    private LocalDateTime seenAt ;
    @ManyToOne
    @JoinColumn(name="user_id")
    private User user ;
    @ManyToOne
    @JoinColumn(name="conversation_id")
    private Conversations conversation ;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_message_id")
    private Messenger replyToMessage;
    @OneToMany(mappedBy = "messenger")
    private List<MessengerStatus> messengerStatuses ;
    @Column(name = "edited_at", columnDefinition = "datetime(6)")
    private LocalDateTime editedAt;
    @Column(name = "recalled_at", columnDefinition = "datetime(6)")
    private LocalDateTime recalledAt;
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", columnDefinition = "varchar(40)")
    private MessageType messageType = MessageType.USER;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_reference_id")
    private Story storyReference;
}
