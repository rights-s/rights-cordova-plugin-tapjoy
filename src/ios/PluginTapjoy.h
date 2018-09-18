#import <Cordova/CDV.h>
#import <Tapjoy/Tapjoy.h>
#import <Tapjoy/TJPlacement.h>

@interface Placement : NSObject

@property NSString *name;
@property CDVInvokedUrlCommand* command;
@property id <CDVCommandDelegate> commandDelegate;

@property NSString *callbackId;
@property TJPlacement *placement;

- (id) initPlacementWithName:(NSString*)Name Command:(CDVInvokedUrlCommand*) Command CommandDelegate:(id <CDVCommandDelegate>) CommandDelegate;

- (void) requestContent:(NSString*)CallbackId;
- (void) showContent:(NSString*)CallbackId;
- (void) showVideoContent:(NSString*)CallbackId;

- (void)requestDidSucceed:(TJPlacement*)placement;// Called when the content request returns from Tapjoy's servers. Does not necessarily mean that content is available.
- (void)requestDidFail:(TJPlacement*)placement error:(NSError*)error;
- (void)contentIsReady:(TJPlacement*)placement; //Called when the content is actually available to display.
- (void)contentDidAppear:(TJPlacement*)placement;
- (void)contentDidDisappear:(TJPlacement*)placement;
@end


@interface PluginTapjoy : CDVPlugin

@property NSMutableArray *placements;
@property NSString *setupCallbackId;

- (void) setup:(CDVInvokedUrlCommand*) command;
- (void) setUserID:(CDVInvokedUrlCommand*) command;
- (void) setUserLevel:(CDVInvokedUrlCommand*) command;
- (void) setUserCohortVariable:(CDVInvokedUrlCommand*) command;
- (void) createPlacement:(CDVInvokedUrlCommand*) command;
- (void) requestContent:(CDVInvokedUrlCommand*) command;
- (void) showContent:(CDVInvokedUrlCommand*) command;
- (void) showVideoContent:(CDVInvokedUrlCommand*) command;

- (Placement*)findPlacementWithName:(NSString*)Name;
@end
