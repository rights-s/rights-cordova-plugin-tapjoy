#import "PluginTapjoy.h"

@implementation Placement

@synthesize name;
@synthesize command;
@synthesize commandDelegate;

@synthesize callbackId;
@synthesize placement;

- (id) initPlacementWithName:(NSString *)Name Command:(CDVInvokedUrlCommand *)Command CommandDelegate:(id<CDVCommandDelegate>)CommandDelegate {
    self = [super init];
    if (self) {
        name = Name;
        command = Command;
        commandDelegate = CommandDelegate;
        placement = [TJPlacement placementWithName: name delegate:self];
    }

    return self;
}

- (void) requestContent:(NSString *)CallbackId {
    callbackId = CallbackId;
    [placement requestContent];
}

- (void) showContent:(NSString *)CallbackId {
    callbackId = CallbackId;
    if (placement.isContentAvailable) {
        [placement showContentWithViewController:nil];
    }
    else {
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                          messageAsString:@"TJ: Content is not ready."];
        [commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
    }
}

- (void) showVideoContent:(NSString *)CallbackId {
    callbackId = CallbackId;

    // VideoのDeleteを指定する
    placement.videoDelegate = self;

    if (placement.isContentAvailable) {
        [placement showContentWithViewController: nil];
    }
    else {
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                          messageAsString:@"TJ: Content is not available."];
        [commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
    }
}

#pragma mark TJPlacement delegate

- (void) requestDidSucceed:(TJPlacement*)placement {
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                      messageAsString:@"TJ: Request succeed."];
    [commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
}

- (void) requestDidFail:(TJPlacement*)placement error:(NSError*)error {
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                      messageAsString: [error localizedDescription]];
    [commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
}

- (void) contentIsReady:(TJPlacement*)placement {
    NSLog(@"Tapjoy Plugin: Content is ready for %@", placement.placementName);
}

- (void) contentDidAppear:(TJPlacement*)placement {
    NSLog(@"Tapjoy Plugin: Content did appear for %@", placement.placementName);
}

- (void) contentDidDisappear:(TJPlacement*)placement {
    NSLog(@"Tapjoy Plugin: Content did disappear for %@", placement.placementName);
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                      messageAsString:@"JP: Content did disappear."];
    [commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
}

#pragma mark Tapjoy Video

- (void) videoDidStart:(TJPlacement *)placement {
    NSLog(@"Tapjoy Plugin: Video did start for: %@", placement.placementName);
}

- (void) videoDidComplete:(TJPlacement*)placement {
    NSLog(@"Tapjoy Plugin: Video has completed for: %@", placement.placementName);
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                      messageAsString:@"JP: Video has completed."];
    [commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
}

- (void) videoDidFail:(TJPlacement*)placement error:(NSString*)errorMsg {
    NSLog(@"Tapjoy Plugin: Video did fail for: %@ with error: %@", placement.placementName, errorMsg);
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                      messageAsString:@"JP: Video did fail."];
    [commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
}

@end

@implementation PluginTapjoy

@synthesize placements;
@synthesize setupCallbackId;

- (void) setup:(CDVInvokedUrlCommand *)command {
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(tjcConnectSuccess:)
                                                 name:TJC_CONNECT_SUCCESS
                                               object:nil];

    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(tjcConnectFail:)
                                                 name:TJC_CONNECT_FAILED
                                               object:nil];

    [Tapjoy setDebugEnabled:[[command.arguments objectAtIndex:0] boolValue]];
    //If you are not using Tapjoy Managed currency, you would set your own user ID here.
    if (![[command.arguments objectAtIndex:1] isKindOfClass: [NSNull class]]){
        [Tapjoy setUserID: [command.arguments objectAtIndex:1]];
    }
    [Tapjoy connect: [command.arguments objectAtIndex:2]];

    placements = [[NSMutableArray alloc] init];
    setupCallbackId = command.callbackId;
}

- (void) tjcConnectSuccess:(NSNotification*)notifyObj{
    NSLog(@"Tapjoy Plugin: Tapjoy connect Succeeded");
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                      messageAsString: @"TJ: Setup complete"];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:setupCallbackId];
}
- (void) tjcConnectFail:(NSNotification*)notifyObj{
    NSLog(@"Tapjoy Plugin: Tapjoy connect Failed");
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                      messageAsString: @"TJ: Setup failed"];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:setupCallbackId];
}

- (void) setUserID:(CDVInvokedUrlCommand *)command{
    if (![[command.arguments objectAtIndex:0] isKindOfClass: [NSNull class]]) {
        NSLog(@"Tapjoy Plugin: Tapjoy Plugin: Set UserID Succeeded");
        [Tapjoy setUserID: [command.arguments objectAtIndex:0]];

        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                          messageAsString: @"TJ: Set UserID Succeeded"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
    else {
        NSLog(@"Tapjoy Plugin: Set UserID Failed");
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                          messageAsString: @"TJ: Set UserID Failed"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}

- (void) setUserLevel:(CDVInvokedUrlCommand *)command{
    if (![[command.arguments objectAtIndex:0] isKindOfClass: [NSNull class]] && [[command.arguments objectAtIndex:0] intValue] > 0) {
        [Tapjoy setUserLevel: [[command.arguments objectAtIndex:0] intValue]];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                          messageAsString: @"TJ: Set User Level Succeeded"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
    else {
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                          messageAsString: @"TJ: Set User Level Failed"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}

- (void) setUserCohortVariable:(CDVInvokedUrlCommand *)command{
    if (![[command.arguments objectAtIndex:0] isKindOfClass: [NSNull class]] && ![[command.arguments objectAtIndex:1] isKindOfClass: [NSNull class]] &&
        [[command.arguments objectAtIndex:0] intValue] >= 1 && [[command.arguments objectAtIndex:0] intValue] <= 5) {
        [Tapjoy setUserCohortVariable: [[command.arguments objectAtIndex:0] intValue] value:[command.arguments objectAtIndex:1]];

        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                          messageAsString: @"TJ: Set User Cohort Variable Succeeded"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
    else {
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                          messageAsString: @"TJ: Set User Cohort Variable Failed"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}

- (void) createPlacement:(CDVInvokedUrlCommand *)command {
    Placement *placement = [[Placement alloc] initPlacementWithName: [command.arguments objectAtIndex:0] Command:command CommandDelegate:self.commandDelegate];
    [placements addObject:placement];
    [self requestContent:command];
}

- (void) requestContent:(CDVInvokedUrlCommand *)command{
    Placement *placement = [self findPlacementWithName: [command.arguments objectAtIndex:0]];
    [placement requestContent:command.callbackId];
}

- (void) showContent:(CDVInvokedUrlCommand *)command{
    Placement *placement = [self findPlacementWithName: [command.arguments objectAtIndex:0]];
    [placement showContent:command.callbackId];
}

- (void) showVideoContent:(CDVInvokedUrlCommand *)command {
    Placement *placement = [self findPlacementWithName: [command.arguments objectAtIndex:0]];
    [placement showVideoContent:command.callbackId];
}

- (Placement*)findPlacementWithName:(NSString*)Name {
    for (Placement* item in placements) {
        if ([item.name isEqualToString:Name]) {
            return item;
        };
    }
    return nil;
}

@end

